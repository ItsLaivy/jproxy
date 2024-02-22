package codes.laivy.proxy.http.utils;

import codes.laivy.proxy.http.core.SecureHttpRequest;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class HttpUtils {

    private HttpUtils() {
        throw new UnsupportedOperationException();
    }

    public static boolean isHeaderSensitive(@NotNull String headerName) {
        @NotNull String[] sensitiveHeaders = new String[] {
                "Authorization", "WWW-Authenticate", "Proxy-Authenticate", "Proxy-Authorization",
                "Cookie", "Set-Cookie",
                "Age", "Cache-Control", "Clear-Site-Data", "Expires",
                "Last-Modified",
                "ETag",
                "If-Match", "If-Modified-Since", "If-Unmodified-Since",
                "X-Frame-Options", "X-XSS-Protection", "X-Content-Type-Options",
                "Referrer-Policy"
        };

        return Arrays.asList(sensitiveHeaders).contains(headerName);
    }
    public static boolean isSecureData(byte @NotNull [] bytes) {
        if (bytes.length < 3) return false;
        return (bytes[0] == 0x16 && (bytes[1] == 0x03 || bytes[1] == 0x02 || bytes[1] == 0x01 || bytes[1] == 0x00));
    }

    public static @Nullable ContentType getContentType(@NotNull MessageHeaders headers) throws ProtocolException {
        @Nullable ContentType type = null;
        if (headers.containsHeader(HttpHeaders.CONTENT_TYPE)) {
            type = ContentType.parse(headers.getHeader(HttpHeaders.CONTENT_TYPE).getValue());
        }

        return type;
    }
    public static @NotNull String read(@NotNull HttpEntityContainer container, @Nullable Charset charset) throws IOException {
        @NotNull BufferedReader reader = new BufferedReader(new InputStreamReader(container.getEntity().getContent(), charset != null ? charset : StandardCharsets.UTF_8));

        @NotNull StringBuilder stringBuilder = new StringBuilder();
        @NotNull String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        return stringBuilder.toString();
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
     * Creates a response with the status code 400 (Client Error/Bad Request) and a message indicating that the proxy failed to process the client request
     * @return an HTTP response object with the 400 status code and a message
     */
    public static @NotNull HttpResponse clientErrorResponse(@NotNull ProtocolVersion version, @NotNull String message) {
        @NotNull HttpResponse response = new BasicHttpResponse(HttpStatus.SC_BAD_REQUEST, message);
        response.setVersion(version);

        return response;
    }

}