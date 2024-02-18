package codes.laivy.proxy.http.utils;


import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
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
    public static @NotNull HttpResponse successResponse() {
        return new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_OK, "connection established"));
    }

    /**
     * Creates a response with the status code 401 (Unauthorized) and a message indicating that the proxy authentication failed
     * @return an HTTP response object with the 401 status code and a message
     */
    public static @NotNull HttpResponse unauthorizedResponse() {
        return new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_UNAUTHORIZED, "proxy authorization failed"));
    }

    /**
     * Creates a response with the status code 500 (Internal Server Error) and a message indicating that the proxy failed to process the request
     * @return an HTTP response object with the 500 status code and a message
     */
    public static @NotNull HttpResponse errorResponse(@NotNull String message) {
        return new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_INTERNAL_SERVER_ERROR, message));
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