package codes.laivy.proxy.http.utils;


import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.jetbrains.annotations.NotNull;

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

}