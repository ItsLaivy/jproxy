package codes.laivy.proxy.http.impl;

import codes.laivy.proxy.exception.SerializationException;
import codes.laivy.proxy.http.connection.HttpProxyClient;
import codes.laivy.proxy.http.utils.HttpSerializers;
import codes.laivy.proxy.http.utils.HttpUtils;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ConnectionImpl implements HttpProxyClient.Connection {

    // Object

    private final HttpProxyClientImpl httpProxyClient;
    protected final @NotNull Queue<CompletableFuture<HttpResponse>> requestFutures = new ArrayDeque<>();

    private final @NotNull InetSocketAddress address;
    private volatile @Nullable Socket socket;

    protected boolean keepAlive = true;
    protected boolean secure = false;
    protected boolean anonymous = false;

    protected ConnectionImpl(HttpProxyClientImpl httpProxyClient, @NotNull InetSocketAddress address) {
        this.httpProxyClient = httpProxyClient;
        this.address = address;
    }

    // Address

    @Override
    public @NotNull InetSocketAddress getAddress() {
        return address;
    }

    @Override
    public @Nullable Socket getSocket() {
        return socket;
    }

    @Override
    public boolean isConnected() {
        @Nullable Socket socket = getSocket();
        return socket != null && socket.isConnected();
    }

    @Override
    public synchronized void connect() throws IOException {
        if (isConnected()) {
            throw new IllegalStateException("this connection has already connected");
        }

        @NotNull SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);

        channel.bind(new InetSocketAddress(httpProxyClient.getProxy().address().getAddress(), 0));
        channel.connect(getAddress());

        if (!channel.finishConnect()) {
            throw new ConnectException("cannot connect to the destination");
        }

        new Thread(() -> {
            while (channel.isConnected()) {
                @NotNull HttpResponse response;

                try {
                    @NotNull ByteBuffer buffer = ByteBuffer.allocate(4096); // 4KB Buffer
                    @NotNull StringBuilder builder = new StringBuilder();
                    int read = channel.read(buffer);

                    if (read == -1) {
                        close();
                        continue;
                    } else if (read == 0) {
                        continue;
                    } else while (read > 0) {
                        buffer.flip();
                        builder.append(StandardCharsets.UTF_8.decode(buffer));
                        buffer.clear();

                        read = channel.read(buffer);
                    }

                    try {
                        System.out.println("Read on connection: '" + builder.toString().replaceAll("\r", "").replaceAll("\n", " ") + "'");
                        buffer = ByteBuffer.wrap(builder.toString().getBytes(StandardCharsets.UTF_8));
                        response = HttpSerializers.getHttpResponse().deserialize(buffer);
                    } catch (@NotNull SerializationException e) {
                        response = HttpUtils.clientErrorResponse(HttpVersion.HTTP_1_1, "Bad Request - " + e.getMessage());
                    }
                } catch (IOException e) {
                    response = HttpUtils.clientErrorResponse(HttpVersion.HTTP_1_1, "Cannot process request");
                }

                try {
                    @Nullable CompletableFuture<HttpResponse> future = requestFutures.poll();

                    if (future == null) {
                        continue; // Discard the response
                    }

                    future.complete(response);
                } catch (@NotNull Throwable ignore) {
                }
            }
        }, "Http Proxy Client '" + httpProxyClient.getAddress() + "' connection #" + httpProxyClient.connectionCount.get()).start();

        this.socket = channel.socket();
    }

    @Override
    public synchronized void close() throws IOException {
        @Nullable Socket socket = getSocket();

        if (socket != null && socket.isConnected()) {
            socket.close();
            this.socket = null;
        }

        for (CompletableFuture<HttpResponse> future : requestFutures) {
            future.completeExceptionally(new InterruptedException("connection closed"));
        }
        requestFutures.clear();
    }

    // Settings

    @Override
    public boolean isKeepAlive() {
        return keepAlive;
    }

    @Override
    public boolean isAnonymous() {
        return anonymous;
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    // Modules

    protected @NotNull Executor getExecutor(@NotNull HttpRequest request) {
        return httpProxyClient.getExecutor(request);
    }

    @Override
    public @NotNull CompletableFuture<HttpResponse> write(@NotNull HttpRequest request) throws IOException, SerializationException {
        @Nullable Socket socket = getSocket();

        if (socket == null || !socket.isConnected() || socket.isClosed()) {
            throw new IllegalStateException("this http proxy connection hasn't connected");
        }

        @NotNull CompletableFuture<HttpResponse> future = new CompletableFuture<>();
        requestFutures.add(future);

        try {
            @NotNull ByteBuffer buffer = HttpSerializers.getHttpRequest().serialize(request);
            socket.getChannel().write(buffer);
            System.out.println("Write: '" + new String(buffer.array()).replaceAll("\r", "").replaceAll("\n", " ") + "'");
        } catch (@NotNull Throwable throwable) {
            requestFutures.remove(future);
            future.completeExceptionally(throwable);
        }

        return future;
    }
}
