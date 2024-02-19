package codes.laivy.proxy.http.utils;

import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

public final class HttpUtils {

    private HttpUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a response with the status code 200 (Success) and a message indicating that the proxy validation occurred and the client can use the proxy
     * @return an HTTP response object with the 200 status code and a message
     */
    public static @NotNull HttpResponse successResponse(@NotNull ProtocolVersion version) {
        @NotNull HttpResponse response = new BasicHttpResponse(HttpStatus.SC_OK, "connection established");
        response.setVersion(version);

        return response;
    }

    /**
     * Creates a response with the status code 401 (Unauthorized) and a message indicating that the proxy authentication failed
     * @return an HTTP response object with the 401 status code and a message
     */
    public static @NotNull HttpResponse unauthorizedResponse(@NotNull ProtocolVersion version) {
        @NotNull HttpResponse response = new BasicHttpResponse(HttpStatus.SC_UNAUTHORIZED, "proxy authorization failed");
        response.setVersion(version);

        return response;
    }

    /**
     * Creates a response with the status code 400 (Client Error) and a message indicating that the proxy failed to process the client request
     * @return an HTTP response object with the 400 status code and a message
     */
    public static @NotNull HttpResponse clientErrorResponse(@NotNull ProtocolVersion version, @NotNull String message) {
        @NotNull HttpResponse response = new BasicHttpResponse(HttpStatus.SC_CLIENT_ERROR, message);
        response.setVersion(version);

        return response;
    }

    public static @NotNull InetSocketAddress getAddress(@Nullable InetSocketAddress previous, @NotNull String path) throws URISyntaxException {
        if (path.startsWith("/")) {
            if (previous == null) {
                throw new IllegalArgumentException("invalid path destination without a previous valid address");
            } else {
                @NotNull URI uri = new URI(null, null, previous.getHostName(), previous.getPort(), path, null, null);
                return new InetSocketAddress(uri.getHost(), uri.getPort());
            }
        } else {
            @NotNull URI uri = new URI(path);
            return new InetSocketAddress(uri.getHost(), uri.getPort() == -1 ? 80 : uri.getPort());
        }
    }

}