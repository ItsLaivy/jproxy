package codes.laivy.proxy.http.utils;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.ProtocolVersion;
import org.apache.http.annotation.Experimental;
import org.apache.http.message.BasicHttpRequest;
import org.jetbrains.annotations.NotNull;

public final class HttpUtils {

    private HttpUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts a raw string of an HTTP (hypertext transfer protocol) request into an HttpRequest object
     * It reads the headers in a different way, it first checks if the header has ": " (a space after the colon), if not it uses the standard ":"
     *
     * @throws HttpException if the request string are broken
     * 
     * @param request a raw HTTP request
     * @return an HttpRequest object
     * @author Daniel Richard (Laivy)
     * @since 1.0-SNAPSHOT
     */
    @Experimental
    public static @NotNull HttpRequest parseRequest(final String request) throws HttpException {
        @NotNull HttpRequest httpRequest;
        @NotNull String[] requestLines;

        try {
            // Request basics
            requestLines = request.split("\n");
            @NotNull String[] requestLine = requestLines[0].split(" ");
            // Method and uri
            @NotNull String method = requestLine[0];
            @NotNull String uri = requestLine[1];
            // Version
            @NotNull String version = requestLine[2];
            @NotNull String[] versionSplit = version.split("/");
            int major = Integer.parseInt(versionSplit[1].split("\\.")[0]);
            int minor = Integer.parseInt(versionSplit[1].split("\\.")[1]);
            // Create request
            httpRequest = new BasicHttpRequest(method, uri, new ProtocolVersion(versionSplit[0], major, minor));
        } catch (@NotNull Throwable throwable) {
            throw new HttpException("cannot read request basics", throwable);
        }

        // Parameters
        for (int i = 1; i < requestLines.length; i++) {
            @NotNull String header = requestLines[i];

            try {
                @NotNull String[] headerParts;

                if (header.contains(": ")) {
                    headerParts = requestLines[i].split(": ");
                } else if (header.contains(":")) {
                    headerParts = requestLines[i].split(":");
                } else {
                    throw new HttpException("header without ':' separator");
                }

                httpRequest.addHeader(headerParts[0], headerParts[1]);
            } catch (@NotNull Throwable throwable) {
                throw new HttpException(header, throwable);
            }
        }

        return httpRequest;
    }

}
