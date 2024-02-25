package codes.laivy.proxy.http.impl;

import codes.laivy.proxy.http.connection.Connection;
import codes.laivy.proxy.http.connection.HttpProxyClient;
import codes.laivy.proxy.http.core.HttpAuthorization;
import codes.laivy.proxy.http.core.HttpStatus;
import codes.laivy.proxy.http.core.URIAuthority;
import codes.laivy.proxy.http.core.headers.Header;
import codes.laivy.proxy.http.core.headers.HeaderKey;
import codes.laivy.proxy.http.core.protocol.HttpVersion;
import codes.laivy.proxy.http.core.request.HttpRequest;
import codes.laivy.proxy.http.core.response.HttpResponse;
import io.netty.util.concurrent.ThreadPerTaskExecutor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class HttpProxyClientImpl implements HttpProxyClient {

    // Static initializers

    // todo: enhance this
    public static @NotNull HeaderKey ANONYMOUS_HEADER = HeaderKey.create("X-Anonymous", Pattern.compile("^(?i)(true|false)$"));

    // Initializers

    protected final @NotNull AtomicInteger connectionCount = new AtomicInteger(0);

    // Default executor used on #getExecutor
    private final @NotNull ThreadPerTaskExecutor executor = new ThreadPerTaskExecutor(new ThreadFactory() {

        private final @NotNull AtomicInteger count = new AtomicInteger(0);

        @Override
        public @NotNull Thread newThread(@NotNull Runnable r) {
            @NotNull Thread thread = new Thread(r);
            thread.setDaemon(false);
            thread.setName("Http Proxy '" + proxy.address() + "' process #" + count.incrementAndGet() + " of client '" + getAddress() + "'");

            return thread;
        }
    });

    // Object

    private final @NotNull HttpProxyImpl proxy;
    private final @NotNull List<Connection> connections = new ArrayList<>();

    private final @NotNull SocketChannel channel;
    private volatile @Nullable Socket destination;

    private final @NotNull InetSocketAddress address;

    protected boolean session = true;
    protected boolean keepAlive = true;
    protected boolean authenticated;

    public HttpProxyClientImpl(@NotNull HttpProxyImpl proxy, @NotNull SocketChannel channel) {
        this.proxy = proxy;
        this.authenticated = proxy.getAuthentication() == null;

        this.channel = channel;
        this.address = new InetSocketAddress(channel.socket().getInetAddress(), channel.socket().getPort());
    }

    // Addresses

    @Override
    public final @NotNull InetSocketAddress getAddress() {
        return address;
    }

    // Connection

    @ApiStatus.OverrideOnly
    protected @NotNull Executor getExecutor(@NotNull HttpRequest request) {
        return executor;
    }

    // Proxy

    @Override
    public final @NotNull HttpProxyImpl getProxy() {
        return proxy;
    }
    @Override
    public @NotNull Socket getSocket() {
        return channel.socket();
    }

    // Settings

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }
    @Override
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
    @Override
    public boolean canSession() {
        return session;
    }

    @Override
    public @NotNull Connection @NotNull [] getConnections() {
        return connections.toArray(new Connection[0]);
    }
    protected @NotNull Optional<Connection> getConnection(@NotNull InetSocketAddress address) {
        return Arrays.stream(getConnections()).filter(connection -> connection.getAddress().equals(address)).findFirst();
    }
    protected @NotNull Connection createConnection(@NotNull InetSocketAddress address, boolean anonymous, boolean keepAlive) throws IOException {
        @NotNull ConnectionImpl instance = new ConnectionImpl(this, address);

        instance.keepAlive = keepAlive;
        instance.anonymous = anonymous;

        instance.connect();

        connectionCount.set(connectionCount.get() + 1);
        connections.add(instance);
        return instance;
    }

    @Override
    public final boolean isKeepAlive() {
        return keepAlive;
    }

    // Loaders

    @Override
    public void close() throws IOException {
        getProxy().getClients().remove(this);

        for (@NotNull Connection connection : getConnections()) {
            try {
                connection.close();
            } catch (@NotNull Throwable ignore) {
            }
        }

        getSocket().close();
        this.destination = null;
    }

    // Modules

    @Override
    public @Nullable HttpRequest read() throws IOException, ParseException {
        @NotNull ByteBuffer buffer = ByteBuffer.allocate(4096); // 4KB Buffer

        @NotNull SocketChannel channel = getSocket().getChannel();
        @NotNull StringBuilder builder = new StringBuilder();

        int read = channel.read(buffer);

        if (read == -1) {
            close();
            return null;
        } else if (read == 0) {
            return null;
        } else while (read > 0) {
            buffer.flip();
            builder.append(StandardCharsets.UTF_8.decode(buffer));
            buffer.clear();

            read = channel.read(buffer);
        }

        byte[] bytes = builder.toString().getBytes();

        @NotNull Optional<@NotNull HttpVersion> optional = Arrays.stream(HttpVersion.getVersions()).filter(v -> v.getFactory().getRequest().isCompatible(this, bytes)).findFirst();
        if (!optional.isPresent()) {
            throw new ParseException("invalid http request", 0);
        }

        System.out.println("Read brute: '" + new String(buffer.array()).replaceAll("\r", "").replaceAll("\n", " ") + "'");
        @NotNull HttpRequest request = optional.get().getFactory().getRequest().parse(this, bytes);
        System.out.println("Read parse: '" + new String(request.getVersion().getFactory().getRequest().wrap(request)).replaceAll("\r", "").replaceAll("\n", " ") + "'");

        request.getHeaders().first(HeaderKey.PROXY_CONNECTION).ifPresent(header -> this.keepAlive = header.getValue().equalsIgnoreCase("keep-alive"));
        request.getHeaders().remove(HeaderKey.PROXY_CONNECTION);

        return request;
    }

    @Override
    public void write(@NotNull HttpResponse response) throws IOException {
        getSocket().getChannel().write(ByteBuffer.wrap(response.getBytes()));
        System.out.println("Write to client: '" + new String(response.getBytes()).replaceAll("\r", "").replaceAll("\n", " ") + "'");
    }
    @Override
    public @NotNull CompletableFuture<HttpResponse> request(@NotNull HttpRequest request) throws IOException, ParseException {
        @NotNull CompletableFuture<HttpResponse> future = new CompletableFuture<>();

        @NotNull HttpRequest clone = HttpRequest.create(request.getVersion(), request.getMethod(), null, request.getUri(), request.getCharset(), request.getHeaders(), request.getMessage());
        System.out.println("Clone: '" + new String(clone.getVersion().getFactory().getRequest().wrap(clone)).replaceAll("\r", "").replaceAll("\n", " ") + "'");

        boolean anonymous = clone.getHeaders().contains(ANONYMOUS_HEADER) && clone.getHeaders().last(ANONYMOUS_HEADER).orElseThrow(NullPointerException::new).getValue().equalsIgnoreCase("true");
        clone.getHeaders().remove(ANONYMOUS_HEADER);

        CompletableFuture.runAsync(() -> {
            try {
                @Nullable Header host = clone.getHeaders().first(HeaderKey.HOST).orElse(null);

                if (host == null) {
                    future.complete(HttpStatus.BAD_REQUEST.createResponse(clone.getVersion()));
                } else {
                    @Nullable HttpAuthorization authorization = getProxy().getAuthentication();
                    if (!isAuthenticated() && authorization != null) {
                        @Nullable HttpResponse authResponse = authorization.validate(this, clone);

                        if (authResponse != null) {
                            future.complete(authResponse);
                            return;
                        } else {
                            setAuthenticated(true);
                        }
                    }

                    try {
                        @NotNull URIAuthority authority = URIAuthority.parse(clone.getHeaders().first(HeaderKey.HOST).orElseThrow(NullPointerException::new).getValue());
                        @Nullable Connection connection = getConnection(InetSocketAddress.createUnresolved(authority.getHostName(), authority.getPort())).orElse(null);

                        if (connection != null) {
                            future.complete(connection.write(clone).get());
                        } else if (!canSession()) {
                            System.out.println("B");
                            future.complete(HttpStatus.BAD_REQUEST.createResponse(clone.getVersion()));
                        } else try { // Create new connection
                            boolean keepAlive = !clone.getHeaders().contains(HeaderKey.CONNECTION) || clone.getHeaders().last(HeaderKey.CONNECTION).orElseThrow(NullPointerException::new).getValue().equalsIgnoreCase("keep-alive");
                            @NotNull Connection instance = createConnection(authority.getAddress(), anonymous, keepAlive);
                            // todo: Clone request witho the anonymous things

                            instance.write(clone).whenComplete((done, exception) -> {
                                try {
                                    if (exception != null) future.completeExceptionally(exception);
                                    else future.complete(done);

                                    if (!instance.isKeepAlive()) {
                                        instance.close();
                                    }
                                } catch (@NotNull Throwable throwable) {
                                    future.completeExceptionally(throwable);
                                }
                            });
                        } catch (@NotNull Throwable ignore) {
                            // todo: create a debug system
                            ignore.printStackTrace();
                            future.complete(HttpStatus.BAD_REQUEST.createResponse(clone.getVersion()));
                        }
                    } catch (@NotNull Throwable throwable) {
                        throwable.printStackTrace();
                        future.completeExceptionally(throwable);
                    }
                }
            } catch (@NotNull Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        }, getExecutor(clone));

        return future;
    }

    // Natives

    @Override
    public boolean equals(@Nullable Object object) {
        if (this == object) return true;
        if (!(object instanceof HttpProxyClientImpl)) return false;
        @NotNull HttpProxyClientImpl that = (HttpProxyClientImpl) object;
        return Objects.equals(getProxy(), that.getProxy()) && Objects.equals(getAddress(), that.getAddress());
    }
    @Override
    public int hashCode() {
        return Objects.hash(getProxy(), getAddress());
    }

}
