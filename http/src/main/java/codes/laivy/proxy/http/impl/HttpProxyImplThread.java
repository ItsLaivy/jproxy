package codes.laivy.proxy.http.impl;

import codes.laivy.proxy.http.connection.HttpProxyClient;
import codes.laivy.proxy.http.core.HttpStatus;
import codes.laivy.proxy.http.core.request.HttpRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

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
                        @Nullable SocketChannel clientSocket = null;

                        try {
                            // Accept the socket
                            clientSocket = server.accept().getChannel();
                            // Configure the socket
                            clientSocket.configureBlocking(false);
                            clientSocket.register(selector, SelectionKey.OP_READ);
                            // Create the proxy client
                            @NotNull HttpProxyClientImpl client = new HttpProxyClientImpl(getProxy(), clientSocket);
                            getProxy().getClients().add(client);
                        } catch (@NotNull Throwable throwable) {
                            getUncaughtExceptionHandler().uncaughtException(this, throwable);

                            if (clientSocket != null) {
                                try { clientSocket.close(); } catch (IOException ignore) {}
                            }
                        }
                    }
                    if (isReadable(key)) {
                        @NotNull SocketChannel clientChannel = (SocketChannel) key.channel();

                        try {
                            @Nullable HttpProxyClient client = getProxy().getClients().stream().filter(socket -> socket.getSocket().equals(clientChannel.socket())).findFirst().orElse(null);

                            if (client == null) {
                                clientChannel.close();
                            } else {
                                @NotNull Socket socket = clientChannel.socket();
                                @Nullable HttpRequest request = client.read();

                                if (request == null) {
                                    client.close();
                                } else {
                                    @NotNull HttpRequest clone = HttpRequest.create(request.getVersion(), request.getMethod(), null, request.getUri(), request.getCharset(), request.getHeaders(), request.getMessage());
                                    System.out.println("Clone: '" + new String(clone.getVersion().getFactory().getRequest().wrap(clone)).replaceAll("\r", "").replaceAll("\n", " ") + "'");

                                    client.request(clone).whenComplete((done, exception) -> {
                                        // todo: look this
                                        if (exception != null) {
                                            try {
                                                client.write(HttpStatus.BAD_REQUEST.createResponse(clone.getVersion()));
                                            } catch (@NotNull Exception ignore) {}
                                        } else try {
                                            client.write(done);
                                        } catch (@NotNull Throwable throwable) {
                                            throwable.printStackTrace();
                                        }
                                    });
                                }
                            }
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