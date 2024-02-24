package codes.laivy.proxy.http.connection;

import codes.laivy.proxy.http.core.request.HttpRequest;
import codes.laivy.proxy.http.core.response.HttpResponse;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.ParseException;
import java.util.concurrent.CompletableFuture;

public interface Connection extends AutoCloseable {

    @Contract(pure = true)
    @NotNull HttpProxyClient getClient();

    @Contract(pure = true)
    @NotNull InetSocketAddress getAddress();

    @Nullable Socket getSocket();

    void connect() throws IOException;

    boolean isConnected();

    boolean isKeepAlive();

    boolean isSecure();

    boolean isAnonymous();

    @NotNull CompletableFuture<HttpResponse> write(@NotNull HttpRequest request) throws IOException, ParseException;

}
