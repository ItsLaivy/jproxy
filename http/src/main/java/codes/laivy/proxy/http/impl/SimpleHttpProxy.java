package codes.laivy.proxy.http.impl;

import codes.laivy.proxy.http.HttpProxy;
import codes.laivy.proxy.http.connection.HttpProxyClient;
import codes.laivy.proxy.http.core.HttpAuthorization;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Collection;

public class SimpleHttpProxy extends HttpProxy {

    // Object

    private final @NotNull Collection<HttpProxyClient> clients = new HttpProxyClients();

    protected volatile @Nullable ServerSocket server;
    protected @Nullable Selector selector;
    protected @Nullable Thread thread;

    // Constructor

    public SimpleHttpProxy(@NotNull InetSocketAddress address, @Nullable HttpAuthorization authorization) {
        super(address, authorization);
    }

    // Getters

    @Override
    public @NotNull Collection<HttpProxyClient> getClients() {
        return clients;
    }

    public final @Nullable Thread getThread() {
        return thread;
    }

    /**
     * @return the selector object that handles the proxy connections or null if the proxy is not running
     */
    public final @Nullable Selector getSelector() {
        return selector;
    }

    // Natives

    @Override
    public final @Nullable ServerSocket getServer() {
        return server;
    }

    // Loaders

    @Override
    public synchronized boolean start() throws Exception {
        @Nullable ServerSocket server = getServer();

        if ((server != null && server.isBound()) || selector != null) {
            return false;
        }

        this.selector = Selector.open();

        // Socket
        @NotNull ServerSocketChannel channel = ServerSocketChannel.open();
        channel.configureBlocking(false);
        this.server = channel.socket();

        channel.socket().bind(address());
        channel.register(getSelector(), SelectionKey.OP_ACCEPT);

        this.thread = new HttpSimpleProxyThread(this);
        this.thread.start();

        return true;
    }

    @Override
    public synchronized boolean stop() throws Exception {
        @Nullable ServerSocket server = getServer();

        if ((server == null || !server.isBound()) || getSelector() == null || this.thread == null) {
            return false;
        }

        // Close clients
        for (@NotNull HttpProxyClient client : getClients()) {
            try {
                client.close();
            } catch (@NotNull Throwable ignore) {
            }
        }

        this.thread.interrupt();

        server.close();
        getSelector().close();

        this.selector = null;
        this.thread = null;
        this.server = null;

        return true;
    }

}
