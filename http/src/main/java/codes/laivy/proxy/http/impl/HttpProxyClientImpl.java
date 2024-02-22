package codes.laivy.proxy.http.impl;

import codes.laivy.proxy.exception.SerializationException;
import codes.laivy.proxy.http.HttpProxy;
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
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static codes.laivy.proxy.http.utils.HttpSerializers.getHttpResponse;
import static codes.laivy.proxy.http.utils.HttpUtils.clientErrorResponse;
import static codes.laivy.proxy.http.utils.HttpUtils.successResponse;

public class HttpProxyClientImpl implements HttpProxyClient {

    // Static initializers

    // Default executor used on #getExecutor
    private final @NotNull ThreadPerTaskExecutor executor = new ThreadPerTaskExecutor(new ThreadFactory() {

        private final @NotNull AtomicInteger count = new AtomicInteger(0);

        @Override
        public @NotNull Thread newThread(@NotNull Runnable r) {
            @NotNull Thread thread = new Thread(r);
            thread.setDaemon(false);
            thread.setName("Http Proxy '" + proxy.address() + "' request #" + count.incrementAndGet() + " of client '" + getAddress() + "'");

            return thread;
        }
    });

    // Object

    private final @NotNull HttpProxy proxy;

    private final @NotNull SocketChannel channel;
    private volatile @Nullable Socket destination;

    private final @NotNull InetSocketAddress address;

    protected boolean keepAlive = true;
    protected boolean secure = false;
    protected boolean anonymous = false;
    protected boolean authenticated;

    public HttpProxyClientImpl(@NotNull HttpProxy proxy, @NotNull SocketChannel channel) {
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

    @Override
    public @Nullable Socket getDestination() {
        return destination;
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
    public boolean isSecure() {
        return secure;
    }
    @Override
    public boolean isAnonymous() {
        return anonymous;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }
    @Override
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    @Override
    public @NotNull Connection @NotNull [] getConnections() {
        return new Connection[0];
    }
    @Override
    public final boolean isKeepAlive() {
        return keepAlive;
    }

    // Loaders

    @Override
    public void close() throws IOException {
        getProxy().getClients().remove(this);

        @Nullable Socket destination = getDestination();
        if (destination != null) {
            destination.close();
        }

        getSocket().close();
        this.destination = null;
    }

    // Modules

    @Override
    public @NotNull HttpRequest read() throws IOException, SerializationException {
        @NotNull SocketChannel channel = getSocket().getChannel();
        @NotNull ByteBuffer buffer = ByteBuffer.allocate(4096); // 4KB Buffer
        @NotNull StringBuilder stringBuilder = new StringBuilder();

        int read = channel.read(buffer);

        if (read == -1) {
            close();
        } else while (read > 0) {
            buffer.flip();
            stringBuilder.append(StandardCharsets.UTF_8.decode(buffer));
            buffer.clear();

            read = channel.read(buffer);
        }

        buffer = ByteBuffer.wrap(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
        @NotNull HttpRequest request = HttpSerializers.getHttpRequest().deserialize(buffer);

        if (request instanceof SecureHttpRequest) {
            // todo: secure http request
            return request;
        } else {
            try {
                // Connection header
                if (request.containsHeader(HttpHeaders.PROXY_CONNECTION)) {
                    this.keepAlive = request.getHeader(HttpHeaders.PROXY_CONNECTION).getValue().equalsIgnoreCase("keep-alive");
                } else if (request.containsHeader(HttpHeaders.CONNECTION)) {
                    this.keepAlive = request.getHeader(HttpHeaders.CONNECTION).getValue().equalsIgnoreCase("keep-alive");
                }

                if (request.containsHeader("Anonymous")) {
                    this.anonymous = request.getHeader("Anonymous").getValue().equalsIgnoreCase("true");
                }
            } catch (@NotNull ProtocolException ignore) {
                this.keepAlive = true;
                this.anonymous = false;
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
            } catch (@NotNull Throwable throwable) {
                throw new SerializationException("cannot clone http request", throwable);
            }

            return clone;
        }
    }

    @Override
    public void write(@NotNull HttpResponse response) throws IOException, SerializationException {
        getSocket().getChannel().write(HttpSerializers.getHttpResponse().serialize(response));
    }
    @Override
    public @NotNull CompletableFuture<HttpResponse> request(@NotNull HttpRequest request) throws IOException, SerializationException {
        @NotNull CompletableFuture<HttpResponse> future = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            try {
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

                            future.complete(successResponse(request.getVersion()));
                            return;
                        } else if (!request.containsHeader(HttpHeaders.HOST)) {
                            future.complete(clientErrorResponse(request.getVersion(), "Bad Request - Missing 'Host' header"));
                            return;
                        }

                    } else if (getConnections().length == 0) {
                        address = getConnections()[0].getAddress();
                    } else {
                        future.complete(clientErrorResponse(request.getVersion(), "Bad Request - SSL request without proxy connection"));
                        return;
                    }


                    @NotNull HttpResponse response = getProxy().request(client, request);
                    System.out.println("Send 5 - '" + new String(getHttpResponse().serialize(response).array()).replaceAll("\r", "").replaceAll("\n", " ") + "'");
                    return response;
                } catch (@NotNull Throwable throwable) {
                    if (isSecure()) {
                        getUncaughtExceptionHandler().uncaughtException(this, throwable);
                        client.close();
                    } else {
                        throwable.printStackTrace();
                        @NotNull HttpResponse response = new BasicHttpResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, "proxy internal error");

                        if (throwable instanceof SerializationException) {
                            response = new BasicHttpResponse(HttpStatus.SC_BAD_REQUEST, "invalid request format");
                        } else if (throwable instanceof ConnectException) {
                            response = new BasicHttpResponse(HttpStatus.SC_BAD_REQUEST, throwable.getMessage());
                        }

                        if (!(request instanceof SecureHttpRequest)) {
                            response.setVersion(request.getVersion());
                        }

                        return response;
                        close();
                    }
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
