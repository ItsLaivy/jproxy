package codes.laivy.proxy.http.impl;

import codes.laivy.proxy.http.HttpProxy;
import codes.laivy.proxy.http.utils.HttpUtils;
import org.apache.http.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpProxyImpl implements HttpProxy {

    // Address

    private final @Nullable Authentication authentication;

    private final @NotNull InetSocketAddress address;
    private final @NotNull Proxy proxy;

    // Socket
    private final @NotNull ServerSocket server;
    protected @Nullable Selector selector;
    protected @Nullable java.lang.Thread thread;

    // Requests
    protected @NotNull Requests requests = Requests.create();

    // Constructor

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


    public final @NotNull Requests getRequests() {
        return requests;
    }
    public final @NotNull ServerSocketChannel getServerChannel() {
        return server.getChannel();
    }

    /**
     * @return the selector object that handles the proxy connections or null if the proxy is not running
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

    @Override
    public @NotNull HttpResponse request(@NotNull Socket socket, @NotNull HttpRequest request) throws IOException, HttpException {
        @NotNull StringBuilder responseString = new StringBuilder();
        @NotNull StringBuilder requestString;

        try {
            // Serialize request
            @NotNull RequestLine line = request.getRequestLine();
            requestString = new StringBuilder(line.getMethod() + " " + line.getUri() + " " + line.getProtocolVersion());
            for (@NotNull Header header : request.getAllHeaders()) {
                requestString.append(header.getName()).append(": ").append(header.getValue());
            }
        } catch (@NotNull Throwable throwable) {
            throw new HttpException("cannot serialize request");
        }

        try (@NotNull Socket requestSocket = new Socket(getHandle())) {
            getRequests().add(requestSocket);
            requestSocket.connect(new InetSocketAddress(request.getRequestLine().getUri(), 80));

            // Send website request
            @NotNull PrintWriter writer = new PrintWriter(requestSocket.getOutputStream());
            writer.println(requestString);

            // Read request
            @NotNull BufferedReader reader = new BufferedReader(new InputStreamReader(requestSocket.getInputStream()));
            @NotNull String line;

            while ((line = reader.readLine()) != null) {
                responseString.append(line).append("\n");
            }

            writer.flush();
            writer.close();

            getRequests().remove(requestSocket);
        }

        try {
            @NotNull HttpResponse response = HttpUtils.parseResponse(responseString.toString());
            return response;
        } catch (@NotNull Throwable throwable) {
            throw new HttpException("invalid response format", throwable);
        }
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

        this.thread = new HttpProxyImpl.Thread(this);
        this.thread.start();

        return true;
    }

    @Override
    public synchronized boolean stop() throws Exception {
        if (!getServerChannel().isOpen() || getSelector() == null || this.thread == null) {
            return false;
        }

        getServerChannel().close();
        getSelector().close();
        this.thread.interrupt();

        this.selector = null;
        this.thread = null;

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

    // Classes

    public interface Requests extends Iterable<Socket> {

        static @NotNull Requests create() {
            return new Requests() {

                private final @NotNull Set<Socket> requests = ConcurrentHashMap.newKeySet();

                @Override
                public boolean add(@NotNull Socket socket) {
                    return requests.add(socket);
                }

                @Override
                public boolean remove(@NotNull Socket socket) {
                    return requests.remove(socket);
                }

                @Override
                public void clear() {
                    requests.clear();
                }

                @Override
                public @NotNull Iterator<Socket> iterator() {
                    return requests.iterator();
                }
            };
        }

        boolean add(@NotNull Socket socket);

        boolean remove(@NotNull Socket socket);

        void clear();

    }

    protected static final class Thread extends java.lang.Thread {

        // Static initializers

        private static final @NotNull AtomicInteger COUNT = new AtomicInteger(0);

        // Object

        private final @NotNull HttpProxyImpl proxy;

        public Thread(@NotNull HttpProxyImpl proxy) {
            setName("Http Proxy");
            setDaemon(true);

            this.proxy = proxy;
        }

        // Getters

        public @NotNull HttpProxyImpl getProxy() {
            return proxy;
        }

        // Natives

        @Override
        public void run() {
            @Nullable Selector selector = getProxy().getSelector();

            if (selector == null) {
                throw new IllegalStateException("the http proxy aren't active");
            }

            while (getProxy().getServer().isBound() && selector.isOpen()) {
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
                            @NotNull ServerSocketChannel server = (ServerSocketChannel) key.channel();

                            try {
                                @NotNull SocketChannel clientSocket = server.accept();

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
                            try {
                                @NotNull ByteBuffer buffer = ByteBuffer.allocate(getProxy().server.getReceiveBufferSize());

                                @NotNull SocketChannel clientChannel = (SocketChannel) key.channel();
                                @NotNull Socket socket = clientChannel.socket();

                                @Nullable HttpRequest request = null;
                                @Nullable HttpResponse response = null;

                                try {
                                    int read = clientChannel.read(buffer);

                                    if (read == -1) {
                                        clientChannel.close();
                                        getProxy().getRequests().remove(socket);
                                    }
                                } catch (@NotNull Throwable throwable) {
                                    response = HttpUtils.errorResponse("cannot read socket data");
                                }

                                try {
                                    @NotNull String message = String.valueOf(buffer.asCharBuffer().array());
                                    request = HttpUtils.parseRequest(message);

                                    try {
                                        @Nullable Authentication authentication = getProxy().getAuthentication();
                                        if (authentication != null && !authentication.validate(socket, request)) {
                                            response = HttpUtils.unauthorizedResponse();
                                        }
                                    } catch (@NotNull Throwable throwable) {
                                        getUncaughtExceptionHandler().uncaughtException(this, throwable);
                                        response = HttpUtils.errorResponse("cannot validate authentication");
                                    }
                                } catch (@NotNull Throwable throwable) {
                                    response = HttpUtils.errorResponse("cannot read http request");
                                }

                                if (response != null && request != null) {
                                    // todo: blocking
                                    response = getProxy().request(socket, request);
                                }

                                @NotNull PrintWriter writer = new PrintWriter(socket.getOutputStream());
                                writer.print(response);

                                writer.close();
                                socket.close();
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

}
