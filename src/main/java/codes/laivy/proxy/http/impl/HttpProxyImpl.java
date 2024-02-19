package codes.laivy.proxy.http.impl;

import codes.laivy.proxy.http.HttpProxy;
import codes.laivy.proxy.http.utils.HttpSerializers;
import codes.laivy.proxy.http.utils.HttpUtils;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static codes.laivy.proxy.http.utils.HttpSerializers.getHttpResponse;

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

    public final @Nullable java.lang.Thread getThread() {
        return thread;
    }
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
    public @NotNull HttpResponse request(@NotNull Socket socket, @NotNull HttpRequest clientRequest) throws IOException, HttpException {
        // Create clone request
        @NotNull HttpRequest request;

        try {
            @NotNull URI uri = clientRequest.getUri();
            request = new BasicHttpRequest(clientRequest.getMethod(), uri.getPath());
            request.setVersion(clientRequest.getVersion());

            for (@NotNull Header header : clientRequest.getHeaders()) {
                request.addHeader(header);
            }

            request.setHeader("Host", HttpUtils.getAddress(null, uri.toString()).getHostName());
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
            getRequests().add(channel.socket());

            @NotNull InetSocketAddress address = HttpUtils.getAddress(null, clientRequest.getUri().toString());
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
        if (getServerChannel().socket().isBound() || selector != null) {
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
        if (!getServerChannel().socket().isBound() || getSelector() == null || this.thread == null) {
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
            setName("Http Proxy #" + proxy.hashCode());
            setDaemon(false);

            this.proxy = proxy;
        }

        // Getters

        public @NotNull HttpProxyImpl getProxy() {
            return proxy;
        }

        // Natives

        @Override
        public void run() {
            // todo: debug
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
                                System.out.println("Connected '" + clientSocket.socket().getPort() + "'");

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

                                @Nullable Authentication authentication = getProxy().getAuthentication();
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

                                try {
                                    if (request.getMethod().equalsIgnoreCase("CONNECT")) {
                                        clientChannel.write(getHttpResponse().serialize(HttpUtils.successResponse(request.getVersion())));
                                        System.out.println("Send 4");
                                    } else try {
                                        System.out.println("Performing request");
                                        // todo: blocking
                                        @NotNull HttpResponse response = getProxy().request(socket, request);
                                        System.out.println("Performing response");
                                        clientChannel.write(getHttpResponse().serialize(response));
                                        System.out.println("Send 5 - '" + new String(getHttpResponse().serialize(response).array()).replaceAll("\r", "").replaceAll("\n", " ") + "'");
                                    } catch (@NotNull Throwable throwable) {
                                        throwable.printStackTrace();
                                    }
                                } catch (@NotNull IOException e) {
                                    e.printStackTrace();
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

}
