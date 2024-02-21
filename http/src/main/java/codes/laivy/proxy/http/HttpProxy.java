package codes.laivy.proxy.http;

import codes.laivy.proxy.ProxyServer;
import codes.laivy.proxy.http.connection.HttpProxyClient;
import codes.laivy.proxy.http.core.HttpAuthorization;
import codes.laivy.proxy.http.impl.HttpProxyImpl;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Collection;

public abstract class HttpProxy extends ProxyServer implements AutoCloseable {

    // Initializers

    public static void main(String[] args) {
    }

    public static @NotNull HttpProxy create(@NotNull InetSocketAddress address, @Nullable HttpAuthorization authorization) throws IOException {
        return new HttpProxyImpl(address, authorization);
    }

    // Object

    private final @Nullable HttpAuthorization authorization;

    protected HttpProxy(@NotNull InetSocketAddress address, @Nullable HttpAuthorization authorization) {
        super(Type.HTTP, address);
        this.authorization = authorization;
    }

    // Getters

    @Override
    public abstract @NotNull Collection<HttpProxyClient> getClients();

    public abstract @Nullable ServerSocket getServer();

    /**
     * A proxy authentication can be used to allow only certain users. If the authentication is null, the user who makes a request using it will not need to provide the authentication details
     * @return the authentication object or null if none is required
     */
    public @Nullable HttpAuthorization getAuthentication() {
        return this.authorization;
    }

    /**
     * This method is responsible for making an HTTP request directly using the proxy, this method does not check
     * the authentication of {@link #getAuthentication()}.
     *
     * @since 1.0-SNAPSHOT
     * @author Daniel Richard (Laivy)
     *
     * @param request the HTTP request to be sent
     * @param client the HTTP client trying to realize the request
     * @return the HTTP response received from the server
     * @throws IOException if there is an error in the socket connection or the input/output streams
     * @throws HttpException if there is an error in the request or response serialization or parsing
     */
    @Blocking
    public abstract @NotNull HttpResponse request(@NotNull HttpProxyClient client, @NotNull HttpRequest request) throws IOException, HttpException;

    // Loaders

    public abstract boolean start() throws Exception;

    public abstract boolean stop() throws Exception;

    @Override
    public void close() throws Exception {
        stop();
    }

    // java.net.Proxy natives

    @Override
    public @NotNull String toString() {
        return "Http Proxy '" + address() + "'";
    }

    // Classes

}
