package codes.laivy.proxy.http.impl;

import codes.laivy.proxy.exception.SerializationException;
import codes.laivy.proxy.http.HttpProxy;
import codes.laivy.proxy.http.connection.HttpProxyClient;
import codes.laivy.proxy.http.core.SecureHttpRequest;
import codes.laivy.proxy.http.utils.HttpSerializers;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class HttpProxyClientImpl implements HttpProxyClient {

    private final @NotNull HttpProxy proxy;

    private final @NotNull SocketChannel channel;
    private volatile @Nullable Socket destination;

    private final @NotNull InetSocketAddress address;

    private @UnknownNullability Connection connection = null;
    private @UnknownNullability Connection proxyConnection = null;

    private boolean secure = false;
    private boolean anonymous = false;
    private boolean authenticated;

    public HttpProxyClientImpl(@NotNull HttpProxy proxy, @NotNull SocketChannel channel) {
        this.proxy = proxy;
        this.authenticated = proxy.getAuthentication() == null;

        this.channel = channel;
        this.address = new InetSocketAddress(channel.socket().getInetAddress(), channel.socket().getPort());
    }

    // Addresses

    @Override
    public @NotNull InetSocketAddress getAddress() {
        return address;
    }

    @Override
    public @Nullable Socket getDestination() {
        return destination;
    }

    // Connection

    @Override
    public @NotNull Connection getConnection() {
        if (connection == null) {
            return Connection.KEEP_ALIVE;
        }

        return connection;
    }
    @Override
    public @NotNull Connection getProxyConnection() {
        if (proxyConnection == null) {
            return Connection.KEEP_ALIVE;
        }

        return proxyConnection;
    }

    // Proxy

    @Override
    public @NotNull HttpProxy getProxy() {
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
            return null;
        } else {
            try {
                // Connection header
                if (request.containsHeader(HttpHeaders.PROXY_CONNECTION)) {
                    this.proxyConnection = request.getHeader(HttpHeaders.PROXY_CONNECTION).getValue().equalsIgnoreCase("close") ? Connection.CLOSE : Connection.KEEP_ALIVE;
                }
                if (request.containsHeader(HttpHeaders.CONNECTION)) {
                    this.connection = request.getHeader(HttpHeaders.CONNECTION).getValue().equalsIgnoreCase("close") ? Connection.CLOSE : Connection.KEEP_ALIVE;
                }
                if (request.containsHeader("Anonymous")) {
                    this.anonymous = request.getHeader("Anonymous").getValue().equalsIgnoreCase("true");
                }
            } catch (@NotNull ProtocolException ignore) {
                this.connection = Connection.KEEP_ALIVE;
                this.proxyConnection = Connection.KEEP_ALIVE;
                this.anonymous = false;
            }

//            if (getProxy().getAuthentication() != null && !authenticated) {
//                @Nullable HttpResponse authError = getProxy().getAuthentication().validate(this, request);
//                this.authenticated = authError == null;
//
//                if (authError != null) {
//                    write(authError);
//                }
//            }

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
    public void write(@NotNull HttpResponse request) throws IOException, SerializationException {
        return;
    }
    @Override
    public @NotNull HttpResponse request(@NotNull HttpRequest request) throws IOException, SerializationException {
        return null;
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
