package codes.laivy.proxy.http.connection;

import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
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
