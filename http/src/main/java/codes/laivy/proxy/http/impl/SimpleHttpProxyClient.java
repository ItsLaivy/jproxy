package codes.laivy.proxy.http.impl;

import codes.laivy.proxy.http.HttpProxy;
import codes.laivy.proxy.http.connection.HttpConnection;
import codes.laivy.proxy.http.connection.HttpProxyClient;
import codes.laivy.proxy.http.core.HttpAuthorization;
import codes.laivy.proxy.http.core.HttpStatus;
import codes.laivy.proxy.http.core.URIAuthority;
import codes.laivy.proxy.http.core.headers.Header;
import codes.laivy.proxy.http.core.headers.HeaderKey;
import codes.laivy.proxy.http.core.protocol.HttpVersion;
import codes.laivy.proxy.http.core.request.HttpRequest;
import codes.laivy.proxy.http.core.response.HttpResponse;
import codes.laivy.proxy.http.exception.UnsupportedHttpVersionException;
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
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

// todo: client timeout
public class SimpleHttpProxyClient implements HttpProxyClient {

    // Initializers

    protected final @NotNull AtomicInteger connectionCount = new AtomicInteger(0);

    // Default executor used on #getExecutor
    private final @NotNull ThreadPerTaskExecutor executor = new ThreadPerTaskExecutor(new ThreadFactory() {

        private final @NotNull AtomicInteger count = new AtomicInteger(0);

        @Override
        public @NotNull Thread newThread(@NotNull Runnable r) {
            @NotNull Thread thread = new Thread(r);
            thread.setDaemon(false);
            thread.setName("SimpleConnection '" + getAddress() + "' process #" + count.incrementAndGet() + " of proxy '" + getProxy().address() + "'");

            return thread;
        }
    });

    // Object

    private final @NotNull HttpProxy proxy;
    private final @NotNull List<HttpConnection> connections = new ArrayList<>();

    private final @NotNull SocketChannel channel;
    private volatile @Nullable Socket destination;

    private final @NotNull InetSocketAddress address;

    protected boolean session = true;
    protected boolean keepAlive = true;
    protected boolean authenticated;

    public SimpleHttpProxyClient(@NotNull HttpProxy proxy, @NotNull SocketChannel channel) {
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
    public final @NotNull HttpProxy getProxy() {
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
    public @NotNull Duration getTimeout() {
        return Duration.ofMinutes(5);
    }

    @Override
    public boolean canSession() {
        return session;
    }

    @Override
    public @NotNull HttpConnection @NotNull [] getConnections() {
        return connections.toArray(new HttpConnection[0]);
    }

    protected @NotNull HttpConnection createConnection(@NotNull InetSocketAddress address, boolean anonymous, boolean keepAlive) throws IOException {
        @NotNull SimpleHttpConnection instance = new SimpleHttpConnection(this, address);

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

        for (@NotNull HttpConnection connection : getConnections()) {
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
    public @Nullable HttpRequest read() throws IOException, UnsupportedHttpVersionException, ParseException {
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
            throw new UnsupportedHttpVersionException("http version not supported by proxy server");
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
        if (request.getAuthority() != null && request.getAuthority().getUserInfo() != null) {
            clone.getHeaders().add(Header.create(HeaderKey.AUTHORIZATION, "Basic " + request.getAuthority().getUserInfo()));
        }

        // Anonymous headers
        boolean anonymous = clone.getHeaders().contains(HeaderKey.ANONYMOUS_HEADER) && clone.getHeaders().last(HeaderKey.ANONYMOUS_HEADER).orElseThrow(NullPointerException::new).getValue().equalsIgnoreCase("true");
        clone.getHeaders().remove(HeaderKey.ANONYMOUS_HEADER);

        // todo: anonymous headers
        //

        System.out.println("Clone: '" + new String(clone.getVersion().getFactory().getRequest().wrap(clone)).replaceAll("\r", "").replaceAll("\n", " ") + "'");

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
                        @Nullable HttpConnection connection = getConnection(authority.getAddress()).orElse(null);

                        if (connection != null) {
                            // todo: add request timeout
                            future.complete(connection.write(clone).get(connection.getTimeout().toMillis(), TimeUnit.MILLISECONDS));
                        } else if (!canSession()) {
                            future.complete(HttpStatus.BAD_REQUEST.createResponse(clone.getVersion()));
                        } else try { // Create new connection
                            boolean keepAlive = !clone.getHeaders().contains(HeaderKey.CONNECTION) || clone.getHeaders().last(HeaderKey.CONNECTION).orElseThrow(NullPointerException::new).getValue().equalsIgnoreCase("keep-alive");
                            connection = createConnection(authority.getAddress(), anonymous, keepAlive);

                            future.complete(connection.write(clone).get(connection.getTimeout().toMillis(), TimeUnit.MILLISECONDS));
                        } catch (@NotNull Throwable e) {
                            future.completeExceptionally(e);
                        }
                    } catch (@NotNull Throwable throwable) {
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
        if (!(object instanceof SimpleHttpProxyClient)) return false;
        @NotNull SimpleHttpProxyClient that = (SimpleHttpProxyClient) object;
        return Objects.equals(getProxy(), that.getProxy()) && Objects.equals(getAddress(), that.getAddress());
    }
    @Override
    public int hashCode() {
        return Objects.hash(getProxy(), getAddress());
    }

}
