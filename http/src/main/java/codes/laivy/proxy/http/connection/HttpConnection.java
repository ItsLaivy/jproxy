package codes.laivy.proxy.http.connection;

import codes.laivy.proxy.http.core.request.HttpRequest;
import codes.laivy.proxy.http.core.response.HttpResponse;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public interface HttpConnection extends Closeable {

    @Contract(pure = true)
    @NotNull HttpProxyClient getClient();

    @Contract(pure = true)
    @NotNull InetSocketAddress getAddress();

    @Nullable Socket getSocket();

    @NotNull Duration getTimeout();

    void connect() throws IOException;

    boolean isConnected();

    boolean isKeepAlive();

    boolean isSecure();

    boolean isAnonymous();

    /**
     * Sends an HTTP response of a previous operation to the client
     *
     * @throws IOException If an input or output error occurs.
     */
    @NotNull CompletableFuture<HttpResponse> write(@NotNull HttpRequest request) throws IOException;

}
