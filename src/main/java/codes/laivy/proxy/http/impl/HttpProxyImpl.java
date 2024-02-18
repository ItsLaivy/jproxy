package codes.laivy.proxy.http.impl;

import codes.laivy.proxy.http.HttpProxy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Objects;

public class HttpProxyImpl implements HttpProxy {

    // Address

    private final @Nullable Authentication authentication;

    private final @NotNull InetSocketAddress address;
    private final @NotNull Proxy proxy;

    // Socket
    private final @NotNull ServerSocket server;
    protected @Nullable Selector selector;

    public HttpProxyImpl(@Nullable Authentication authentication, @NotNull InetSocketAddress address) throws IOException {
        this(authentication, address, ServerSocketChannel.open());
    }
    protected HttpProxyImpl(@Nullable Authentication authentication, @NotNull InetSocketAddress address, @NotNull ServerSocketChannel channel) throws IOException {
        this.authentication = authentication;
        this.address = address;

        // Proxy
        this.proxy = new Proxy(Proxy.Type.HTTP, address);
        // Socket
        channel.configureBlocking(false);
        this.server = channel.socket();
    }

    // Getters

    public @NotNull ServerSocketChannel getServerChannel() {
        return server.getChannel();
    }

    /**
     * todo: Javadocs
     * @return null if proxy is not running
     */
    public final @Nullable Selector getSelector() {
        return selector;
    }

    // Natives

    @Override
    public final @NotNull InetSocketAddress getAddress() {
        return address;
    }

    @Override
    public @NotNull Proxy getHandle() {
        return this.proxy;
    }

    @Override
    public final @NotNull ServerSocket getServer() {
        return server;
    }

    @Override
    public final @Nullable Authentication getAuthentication() {
        return authentication;
    }

    // Loaders

    @Override
    public synchronized boolean start() throws Exception {
        if (getServerChannel().isOpen() || selector != null) {
            return false;
        }

        this.selector = Selector.open();

        getServerChannel().bind(getAddress());
        getServerChannel().register(getSelector(), SelectionKey.OP_ACCEPT);

        return true;
    }

    @Override
    public synchronized boolean stop() throws Exception {
        if (!getServerChannel().isOpen() || getSelector() == null) {
            return false;
        }

        getServerChannel().close();
        getSelector().close();

        this.selector = null;
        return true;
    }

    // Equals

    @Override
    public boolean equals(@Nullable Object object) {
        if (this == object) return true;
        if (!(object instanceof HttpProxyImpl)) return false;
        HttpProxyImpl httpProxy = (HttpProxyImpl) object;
        return Objects.equals(getAddress(), httpProxy.getAddress());
    }
    @Override
    public int hashCode() {
        return Objects.hash(getAddress());
    }

}
