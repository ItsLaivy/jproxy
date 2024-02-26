package codes.laivy.proxy.http.impl;

import codes.laivy.proxy.http.connection.HttpConnection;
import codes.laivy.proxy.http.core.HttpStatus;
import codes.laivy.proxy.http.core.protocol.HttpVersion;
import codes.laivy.proxy.http.core.request.HttpRequest;
import codes.laivy.proxy.http.core.response.HttpResponse;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;

public class SimpleHttpConnection implements HttpConnection {

    // Concurrency

    protected final @NotNull Queue<CompletableFuture<HttpResponse>> queue = new ConcurrentLinkedDeque<>();

    // Thread

    protected @Nullable Thread thread;

    // Object

    private final @NotNull SimpleHttpProxyClient client;

    private final @NotNull InetSocketAddress address;
    protected @Nullable Socket socket;

    protected boolean keepAlive = true;
    protected boolean secure = false;
    protected boolean anonymous = false;

    protected SimpleHttpConnection(@NotNull SimpleHttpProxyClient client, @NotNull InetSocketAddress address) {
        this.client = client;
        this.address = address;
    }

    // Getters

    @Override
    @Contract(pure = true)
    public final @NotNull SimpleHttpProxyClient getClient() {
        return client;
    }

    // Address

    @Override
    @Contract(pure = true)
    public final @NotNull InetSocketAddress getAddress() {
        return address;
    }

    @Override
    public @Nullable Socket getSocket() {
        return socket;
    }

    @Override
    public boolean isConnected() {
        @Nullable Socket socket = getSocket();
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    @Override
    public synchronized void connect() throws IOException {
        if (isConnected()) {
            throw new IllegalStateException("this connection has already connected");
        }

        @NotNull SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);

        channel.bind(new InetSocketAddress(getClient().getProxy().address().getHostName(), 0));
        channel.connect(getAddress().isUnresolved() ? new InetSocketAddress(getAddress().getHostName(), getAddress().getPort()) : getAddress());

        if (!channel.finishConnect()) {
            throw new SocketException("cannot initialize connection to " + getAddress().getAddress().getHostName() + ":" + getAddress().getPort());
        }

        this.socket = channel.socket();
        this.thread = new Thread(() -> {
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
                        byte[] bytes = builder.toString().getBytes();

                        @NotNull Optional<@NotNull HttpVersion> optional = Arrays.stream(HttpVersion.getVersions()).filter(v -> v.getFactory().getResponse().isCompatible(client, bytes)).findFirst();
                        if (!optional.isPresent()) {
                            throw new ParseException("invalid http response", 0);
                        }

                        response = optional.get().getFactory().getResponse().parse(client, bytes);
                    } catch (@NotNull ParseException e) {
                        response = HttpResponse.create(new HttpStatus(400, "Bad Request - '" + e.getMessage() + "'"), HttpVersion.HTTP1_1(), StandardCharsets.UTF_8, null);
                    }
                } catch (ClosedChannelException ignore) {
                    continue;
                } catch (IOException e) {
                    response = HttpStatus.BAD_REQUEST.createResponse(HttpVersion.HTTP1_1());
                }

                try {
                    @Nullable CompletableFuture<HttpResponse> future = queue.poll();
                    if (future != null) future.complete(response);
                } catch (@NotNull Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }, "Http Proxy Client '" + getClient().getAddress() + "' connection #" + getClient().connectionCount.get());
        this.thread.start();
    }

    @Override
    public synchronized void close() throws IOException {
        @Nullable Socket socket = getSocket();

        if (socket != null && socket.isConnected()) {
            socket.close();
            this.socket = null;
        }

        // Close thread
        if (this.thread != null) {
            this.thread.interrupt();
        }
        // Close requests
        for (@NotNull CompletableFuture<HttpResponse> future : queue) {
            future.completeExceptionally(new InterruptedException("connection closed"));
        }
        queue.clear();
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
        return getClient().getExecutor(request);
    }

    protected void send(@NotNull HttpRequest request) throws IOException {
        @Nullable Socket socket = getSocket();
        if (socket == null || !isConnected()) {
            connect();
            socket = getSocket();
        }

        // Send request
        @NotNull ByteBuffer buffer = ByteBuffer.wrap(request.getBytes());
        socket.getChannel().write(buffer);
    }

    @Override
    public synchronized @NotNull CompletableFuture<HttpResponse> write(@NotNull HttpRequest request) throws IOException {
        @NotNull CompletableFuture<HttpResponse> future = new CompletableFuture<>();
        queue.add(future);

        if (queue.size() == 1) {
            send(request);
        }

        future.whenComplete((done, exception) -> {
            if (!isKeepAlive()) {
                try {
                    close();
                } catch (IOException ignore) {
                }
            }
        });

        return future;
    }
}
