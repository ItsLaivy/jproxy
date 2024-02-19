package codes.laivy.proxy.http.impl;

import codes.laivy.proxy.http.HttpProxy;
import codes.laivy.proxy.exception.SerializationException;
import codes.laivy.proxy.http.utils.HttpSerializers;
import codes.laivy.proxy.http.utils.HttpUtils;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static codes.laivy.proxy.http.utils.HttpSerializers.getHttpResponse;

class HttpProxyImplThread extends Thread {

    private final @NotNull HttpProxyImpl proxy;

    private final @NotNull Selector selector;
    private final @NotNull ServerSocket server;

    public HttpProxyImplThread(@NotNull HttpProxyImpl proxy) {
        setName("Http Proxy #" + proxy.hashCode());
        setDaemon(false);

        this.proxy = proxy;

        @Nullable Selector selector = getProxy().getSelector();
        @Nullable ServerSocket server = getProxy().getServer();

        if (selector == null || server == null) {
            throw new IllegalStateException("the http proxy aren't active");
        }

        this.selector = selector;
        this.server = server;
    }

    // Getters

    public @NotNull HttpProxyImpl getProxy() {
        return proxy;
    }

    // Natives

    @Override
    public void run() {
        while (server.isBound() && selector.isOpen()) {
            @NotNull Set<SelectionKey> selectedKeys;
            @NotNull Iterator<SelectionKey> keyIterator;

            try {
                @Range(from = 0, to = Integer.MAX_VALUE)
                int readyChannels = selector.select();

                if (readyChannels == 0) continue;

                selectedKeys = selector.selectedKeys();
                keyIterator = selectedKeys.iterator();
            } catch (ClosedSelectorException e) {
                break;
            } catch (IOException e) {
                continue;
            }

            while (keyIterator.hasNext()) {
                try {
                    @NotNull SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (isAcceptable(key)) {
                        try {
                            @NotNull SocketChannel clientSocket = server.accept().getChannel();

                            try {
                                clientSocket.configureBlocking(false);
                                clientSocket.register(selector, SelectionKey.OP_READ);
                            } catch (Exception e) {
                                clientSocket.close();
                            }
                        } catch (@NotNull Throwable throwable) {
                            getUncaughtExceptionHandler().uncaughtException(this, throwable);
                        }
                    }
                    if (isReadable(key)) {
                        @NotNull SocketChannel clientChannel = (SocketChannel) key.channel();

                        try {
                            @NotNull Socket socket = clientChannel.socket();
                            @NotNull ByteBuffer buffer;

                            @NotNull HttpRequest request;

                            try {
                                @NotNull ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                                @NotNull StringBuilder stringBuilder = new StringBuilder();

                                int read = clientChannel.read(readBuffer);

                                if (read == -1) {
                                    clientChannel.close();
                                    continue;
                                } else while (read > 0) {
                                    readBuffer.flip();
                                    stringBuilder.append(StandardCharsets.UTF_8.decode(readBuffer));
                                    readBuffer.clear();

                                    read = clientChannel.read(readBuffer);
                                }

                                buffer = ByteBuffer.wrap(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
                            } catch (@NotNull IOException ignore) {
                                clientChannel.close();
                                continue;
                            }

                            try {
                                request = HttpSerializers.getHttpRequest().deserialize(buffer);
                            } catch (@NotNull Throwable throwable) {
                                clientChannel.close();
                                continue;
                            }

                            @Nullable HttpProxy.Authentication authentication = getProxy().getAuthentication();
                            if (authentication != null && !request.getMethod().equalsIgnoreCase("CONNECT")) {
                                @Nullable HttpResponse authResponse = null;

                                try {
                                    if (!authentication.validate(socket, request)) {
                                        authResponse = HttpUtils.unauthorizedResponse(request.getVersion());
                                    }
                                } catch (@NotNull Throwable throwable) {
                                    getUncaughtExceptionHandler().uncaughtException(this, throwable);
                                    authResponse = HttpUtils.unauthorizedResponse(request.getVersion());
                                }

                                if (authResponse != null) {
                                    clientChannel.write(getHttpResponse().serialize(authResponse));
                                    continue;
                                }
                            }

                            CompletableFuture.runAsync(() -> {
                                try {
                                    if (request.getMethod().equalsIgnoreCase("CONNECT")) {
                                        clientChannel.write(getHttpResponse().serialize(HttpUtils.successResponse(request.getVersion())));
                                        System.out.println("Send 4");
                                    } else {
                                        // todo: blocking
                                        @NotNull HttpResponse response = getProxy().request(socket, request);
                                        clientChannel.write(getHttpResponse().serialize(response));
                                        System.out.println("Send 5 - '" + new String(getHttpResponse().serialize(response).array()).replaceAll("\r", "").replaceAll("\n", " ") + "'");
                                    }
                                } catch (@NotNull SerializationException e) {
                                    try {
                                        clientChannel.write(HttpSerializers.getHttpResponse().serialize(HttpUtils.clientErrorResponse(request.getVersion(), "bad request")));
                                        clientChannel.close();
                                    } catch (@NotNull Throwable ignore) {
                                    }
                                } catch (@NotNull Throwable e) {
                                    getUncaughtExceptionHandler().uncaughtException(HttpProxyImplThread.this, e);
                                }
                            }, getProxy().getExecutor(socket, request));
                        } catch (@NotNull Throwable throwable) {
                            getUncaughtExceptionHandler().uncaughtException(this, throwable);
                        }
                    }
                } catch (CancelledKeyException ignore) {
                }
            }
        }
    }

    private boolean isAcceptable(@NotNull SelectionKey key) {
        return (key.readyOps() & SelectionKey.OP_ACCEPT) != 0;
    }
    private boolean isReadable(@NotNull SelectionKey key) {
        return (key.readyOps() & SelectionKey.OP_READ) != 0;
    }

}