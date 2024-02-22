package codes.laivy.proxy.http.connection;

import codes.laivy.proxy.connection.ProxyClient;
import codes.laivy.proxy.exception.SerializationException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

public interface HttpProxyClient extends ProxyClient {

    @NotNull Socket getSocket();
    @Nullable Socket getDestination();

    boolean isSecure();
    boolean isAnonymous();

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
     * @throws SerializationException If an error occurs trying to serialize the request
     */
    @Nullable HttpRequest read() throws IOException, SerializationException;

    /**
     * Sends an HTTP response of a previous operation to the client
     *
     * @throws IOException If an input or output error occurs.
     * @throws SerializationException If an error occurs trying to serialize the response
     */
    void write(@NotNull HttpResponse response) throws IOException, SerializationException;

    /**
     * Creates an HTTP request to the destination proxy on behalf of the client.
     * @param request The future of the HTTP request to be requested to the destination by the proxy.
     * @return An HTTP response received from the destination by the proxy.
     *
     * @throws IOException If an input or output error occurs.
     * @throws SerializationException If an error occurs trying to deserialize/serialize the response/request
     */
    @NotNull CompletableFuture<HttpResponse> request(@NotNull HttpRequest request) throws IOException, SerializationException;

    // Classes

    interface Connection {

        @NotNull InetSocketAddress getAddress();
        @Nullable Socket getSocket();

        boolean isKeepAlive();

        @NotNull HttpRequest write(@NotNull HttpResponse response) throws IOException, SerializationException;

    }

}
