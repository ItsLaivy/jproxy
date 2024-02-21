package codes.laivy.proxy.http.impl;

import codes.laivy.proxy.http.HttpProxy;
import codes.laivy.proxy.http.connection.HttpProxyClient;
import codes.laivy.proxy.http.utils.HttpSerializers;
import codes.laivy.proxy.http.utils.HttpUtils;
import io.netty.util.concurrent.ThreadPerTaskExecutor;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static codes.laivy.proxy.http.utils.HttpSerializers.getHttpResponse;

// todo: stop all threads when http proxy stop
public class HttpProxyImpl extends HttpProxy {

    // Default executor used on #getExecutor
    private final @NotNull ThreadPerTaskExecutor executor = new ThreadPerTaskExecutor(new ThreadFactory() {

        private final @NotNull AtomicInteger count = new AtomicInteger(0);

        @Override
        public Thread newThread(@NotNull Runnable r) {
            @NotNull Thread thread = new Thread(r);
            thread.setDaemon(false);
            thread.setName("Proxy '" + address() + "' request #");

            return thread;
        }
    });

    // Object

    private final @NotNull Collection<HttpProxyClient> clients = new HttpProxyClients();

    protected volatile @Nullable ServerSocket server;
    protected @Nullable Selector selector;
    protected @Nullable Thread thread;

    // Constructor

    public HttpProxyImpl(@NotNull InetSocketAddress address, @Nullable HttpProxy.Authorization authorization) {
        super(address, authorization);
    }

    // Getters

    @Override
    public @NotNull Collection<HttpProxyClient> getClients() {
        return clients;
    }

    @ApiStatus.OverrideOnly
    public @NotNull Executor getExecutor(@NotNull Socket socket, @NotNull HttpRequest request) {
        return executor;
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

    @Override
    public @NotNull HttpResponse request(@NotNull HttpProxyClient client, @NotNull HttpRequest clientRequest) throws IOException, HttpException {
        // Create clone request
        @NotNull HttpRequest request;

        try {
            @NotNull URI uri = clientRequest.getUri();

            if (clientRequest instanceof HttpEntityContainer) {
                request = new BasicClassicHttpRequest(clientRequest.getMethod(), uri.getPath());
                ((BasicClassicHttpRequest) request).setEntity(((HttpEntityContainer) clientRequest).getEntity());
            } else {
                request = new BasicHttpRequest(clientRequest.getMethod(), uri.getPath());
            }

            request.setVersion(clientRequest.getVersion());

            for (@NotNull Header header : clientRequest.getHeaders()) {
                request.addHeader(header);
            }

            request.setHeader("Host", HttpUtils.getAddress(client.getAddress(), uri.toString()).getHostName());
            if (!request.containsHeader("User-Agent")) {
                request.addHeader("User-Agent", "java-" + System.getProperty("java.version"));
            } if (!request.containsHeader("Connection")) {
                request.addHeader("Connection", "close");
            }
        } catch (@NotNull Throwable throwable) {
            throw new HttpException("cannot create clone request", throwable);
        }

        // Request
        try (@NotNull SocketChannel channel = SocketChannel.open()) {
            @NotNull InetSocketAddress address = HttpUtils.getAddress(client.getAddress(), clientRequest.getUri().toString());
            channel.bind(new InetSocketAddress(address().getAddress(), 0));
            channel.connect(address);

            try {
                // Send website request
                channel.write(HttpSerializers.getHttpRequest().serialize(request));
                System.out.println(": Send: '" + new String(HttpSerializers.getHttpRequest().serialize(request).array()).replaceAll("\r", "").replaceAll("\n", " ") + "'");
            } catch (@NotNull Throwable throwable) {
                throw new HttpException("cannot write http request to destination", throwable);
            }

            try {
                // Read request
                @NotNull ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                @NotNull StringBuilder stringBuilder = new StringBuilder();

                while (channel.read(readBuffer) > 0) {
                    readBuffer.flip();
                    stringBuilder.append(StandardCharsets.UTF_8.decode(readBuffer));
                    readBuffer.clear();
                }

                System.out.println(": Read: '" + stringBuilder.toString().replaceAll("\r", "").replaceAll("\n", " ") + "'");

                @NotNull ByteBuffer buffer = ByteBuffer.wrap(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
                return getHttpResponse().deserialize(buffer);
            } catch (@NotNull Throwable throwable) {
                throw new HttpException("cannot read http response", throwable);
            }
        } catch (URISyntaxException e) {
            throw new HttpException("cannot read uri", e);
        }
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

        this.thread = new HttpProxyImplThread(this);
        this.thread.start();

        return true;
    }

    @Override
    public synchronized boolean stop() throws Exception {
        @Nullable ServerSocket server = getServer();

        if ((server == null || !server.isBound()) || getSelector() == null || this.thread == null) {
            return false;
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
