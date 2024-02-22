package codes.laivy.proxy.http.impl;

import codes.laivy.proxy.exception.SerializationException;
import codes.laivy.proxy.http.connection.HttpProxyClient;
import codes.laivy.proxy.http.core.HttpAuthorization;
import codes.laivy.proxy.http.core.SecureHttpRequest;
import codes.laivy.proxy.http.utils.HttpAddressUtils;
import codes.laivy.proxy.http.utils.HttpSerializers;
import codes.laivy.proxy.http.utils.HttpUtils;
import io.netty.util.concurrent.ThreadPerTaskExecutor;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static codes.laivy.proxy.http.HttpProxy.ANONYMOUS_HEADER;
import static codes.laivy.proxy.http.utils.HttpUtils.clientErrorResponse;
import static codes.laivy.proxy.http.utils.HttpUtils.successResponse;

public class HttpProxyClientImpl implements HttpProxyClient {

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

        connectionCount.set(connectionCount.get() + 1);
        instance.connect();

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
    public @NotNull HttpRequest read() throws IOException, SerializationException {
        @NotNull ByteBuffer buffer = ByteBuffer.allocate(4096); // 4KB Buffer

        @NotNull SocketChannel channel = getSocket().getChannel();
        @NotNull StringBuilder builder = new StringBuilder();

        int read = channel.read(buffer);

        if (read == -1) {
            close();
        } else while (read > 0) {
            buffer.flip();
            builder.append(StandardCharsets.UTF_8.decode(buffer));
            buffer.clear();

            read = channel.read(buffer);
        }

        buffer = ByteBuffer.wrap(builder.toString().getBytes(StandardCharsets.UTF_8));
        System.out.println("Read brute: '" + new String(buffer.array()).replaceAll("\r", "").replaceAll("\n", " ") + "'");
        @NotNull HttpRequest request = HttpSerializers.getHttpRequest().deserialize(buffer);

        if (request instanceof SecureHttpRequest) {
            return request;
        } else {
            try {
                if (request.containsHeader(HttpHeaders.PROXY_CONNECTION)) {
                    // Connection header
                    this.keepAlive = request.getHeader(HttpHeaders.PROXY_CONNECTION).getValue().equalsIgnoreCase("keep-alive");
                }
            } catch (@NotNull ProtocolException ignore) {
                this.keepAlive = true;
            }

            @NotNull HttpRequest clone;

            try {
                if (request instanceof HttpEntityContainer) {
                    @NotNull BasicClassicHttpRequest withBody = new BasicClassicHttpRequest(request.getMethod(), request.getUri().getPath());
                    withBody.setVersion(request.getVersion());
                    withBody.setEntity(((HttpEntityContainer) request).getEntity());

                    clone = withBody;
                } else {
                    @NotNull BasicHttpRequest withBody = new BasicHttpRequest(request.getMethod(), request.getUri().getPath());
                    withBody.setVersion(request.getVersion());

                    clone = withBody;
                }

                for (@NotNull Header header : request.getHeaders()) {
                    clone.addHeader(header);
                }
            } catch (@NotNull Throwable throwable) {
                throw new SerializationException("cannot clone http request", throwable);
            }

            return clone;
        }
    }

    @Override
    public void write(@NotNull HttpResponse response) throws IOException, SerializationException {
        getSocket().getChannel().write(HttpSerializers.getHttpResponse().serialize(response));
        System.out.println("Write to client: '" + new String(HttpSerializers.getHttpResponse().serialize(response).array()).replaceAll("\r", "").replaceAll("\n", " ") + "'");
    }
    @Override
    public @NotNull CompletableFuture<HttpResponse> request(@NotNull HttpRequest request) throws IOException, SerializationException {
        @NotNull CompletableFuture<HttpResponse> future = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            try {
                if (!(request instanceof SecureHttpRequest) && !request.containsHeader("Host")) {
                    future.complete(HttpUtils.clientErrorResponse(request.getVersion(), "Bad Request - Missing 'Host' header"));
                    return;
                }

                @Nullable HttpAuthorization authorization = getProxy().getAuthentication();
                if (!isAuthenticated() && authorization != null && !(request instanceof SecureHttpRequest)) {
                    @Nullable HttpResponse authResponse = authorization.validate(this, request);

                    if (authResponse != null) {
                        future.complete(authResponse);
                        return;
                    } else {
                        setAuthenticated(true);
                    }
                }

                try {
                    @NotNull InetSocketAddress address;

                    if (!(request instanceof SecureHttpRequest)) {
                        address = HttpAddressUtils.getAddressByHost(request.getHeader(HttpHeaders.HOST).getValue());

                        if (request.getMethod().equalsIgnoreCase("CONNECT")) {
                            boolean keepAlive = !request.containsHeader(HttpHeaders.PROXY_CONNECTION) || request.getHeader(HttpHeaders.PROXY_CONNECTION).getValue().equalsIgnoreCase("keep-alive");
                            boolean anonymous = request.containsHeader(ANONYMOUS_HEADER) && request.getHeader(ANONYMOUS_HEADER).getValue().equalsIgnoreCase("true");

                            createConnection(address, anonymous, keepAlive);

                            session = false;
                            future.complete(successResponse(request.getVersion()));
                            return;
                        } else if (!request.containsHeader(HttpHeaders.HOST)) {
                            future.complete(clientErrorResponse(request.getVersion(), "Bad Request - Missing 'Host' header"));
                            return;
                        }
                    } else if (getConnections().length == 0) {
                        address = getConnections()[0].getAddress();
                    } else {
                        future.complete(clientErrorResponse(request.getVersion(), "Bad Request - SSL request without proxy validation!"));
                        return;
                    }

                    @Nullable Connection connection = getConnection(address).orElse(null);

                    if (connection != null) {
                        future.complete(connection.write(request).get());
                    } else if (!canSession()) {
                        future.complete(clientErrorResponse(request.getVersion(), "Bad Request - Cannot have multiples sessions!"));
                    } else try { // Create new connection
                        boolean keepAlive = !request.containsHeader(HttpHeaders.CONNECTION) || request.getHeader(HttpHeaders.CONNECTION).getValue().equalsIgnoreCase("keep-alive");
                        boolean anonymous = request.containsHeader(ANONYMOUS_HEADER) && request.getHeader(ANONYMOUS_HEADER).getValue().equalsIgnoreCase("true");

                        @NotNull Connection instance = createConnection(address, anonymous, keepAlive);
                        // todo: Clone request without the anonymous things

                        instance.write(request).whenComplete((done, exception) -> {
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
                        future.complete(clientErrorResponse(request.getVersion(), "Bad Request - Cannot connect to destination"));
                    }
                } catch (@NotNull Throwable throwable) {
                    @NotNull HttpResponse response = new BasicHttpResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, "proxy internal error");

                    if (throwable instanceof SerializationException) {
                        response = new BasicHttpResponse(HttpStatus.SC_BAD_REQUEST, "invalid request format");
                    } else if (throwable instanceof ConnectException) {
                        response = new BasicHttpResponse(HttpStatus.SC_BAD_REQUEST, throwable.getMessage());
                    }

                    if (!(request instanceof SecureHttpRequest)) {
                        response.setVersion(request.getVersion());
                    }

                    future.complete(response);
                }
            } catch (@NotNull Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        }, getExecutor(request));

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
