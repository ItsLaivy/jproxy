package codes.laivy.proxy.http.connection;

import codes.laivy.proxy.connection.ProxyClient;
import codes.laivy.proxy.http.core.request.HttpRequest;
import codes.laivy.proxy.http.core.response.HttpResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.Socket;
import java.text.ParseException;
import java.util.concurrent.CompletableFuture;

public interface HttpProxyClient extends ProxyClient {

    @NotNull Socket getSocket();

    /**
     * Determines whether this HTTP proxy client is capable of handling multiple requests simultaneously.
     * For instance, it establishes a connection to the proxy and can issue multiple requests to different websites.
     * It always returns false for SSL connections, as an SSL connection cannot be identified if it has more than one communication at a time.
     *
     * @return true if the proxy client can handle multiple requests, false otherwise.
     */
    boolean canSession();

    boolean isAuthenticated();
    void setAuthenticated(boolean authenticated);

    // Connection

    @NotNull Connection @NotNull [] getConnections();
    boolean isKeepAlive();

    // Modules

    /**
     * Blocks the thread waiting for a new HTTP request to the destination through the proxy.
     * @return An HTTP request read from the proxy or null if the connection has closed.
     *
     * @throws IOException If an input or output error occurs.
     * @throws ParseException If an error occurs trying to parse the request
     */
    @Nullable HttpRequest read() throws IOException, ParseException;

    /**
     * Sends an HTTP response of a previous operation to the client
     *
     * @throws IOException If an input or output error occurs.
z     */
    void write(@NotNull HttpResponse response) throws IOException;

    /**
     * Creates an HTTP request to the destination proxy on behalf of the client.
     * @param request The future of the HTTP request to be requested to the destination by the proxy.
     * @return An HTTP response received from the destination by the proxy.
     *
     * @throws IOException If an input or output error occurs.
     * @throws ParseException If an error occurs trying to deserialize/serialize the response/request
     */
    @NotNull CompletableFuture<HttpResponse> request(@NotNull HttpRequest request) throws IOException, ParseException;

    // Classes

}
