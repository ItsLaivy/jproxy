package codes.laivy.proxy.http.impl;

import codes.laivy.proxy.http.HttpProxy;
import codes.laivy.proxy.http.connection.HttpProxyClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.Objects;

public final class HttpProxyClientImpl implements HttpProxyClient {

    private final @NotNull HttpProxy proxy;
    private final @NotNull SocketChannel channel;

    private final @NotNull InetSocketAddress address;
    private @Nullable InetSocketAddress destination;

    private boolean secure = false;

    public HttpProxyClientImpl(@NotNull HttpProxy proxy, @NotNull SocketChannel channel) {
        this.proxy = proxy;
        this.channel = channel;

        this.address = new InetSocketAddress(channel.socket().getInetAddress(), channel.socket().getPort());
        this.destination = null;
    }

    // Addresses

    @Override
    public @NotNull InetSocketAddress getAddress() {
        return address;
    }

    @Override
    public @Nullable InetSocketAddress getDestination() {
        return destination;
    }
    public void setDestination(@NotNull InetSocketAddress destination) {
        this.destination = destination;
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
    public void setSecure(boolean secure) {
        this.secure = secure;
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
