package codes.laivy.proxy.http.utils;

import org.apache.http.*;
import org.apache.http.annotation.Experimental;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class HttpUtils {

    private static final @NotNull Pattern HEADERS_SPLIT_PATTERN = Pattern.compile("(\\S+?):\\s?(.*?)(?=\\s\\S+?:|$)");

    private HttpUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts a raw string of an HTTP (hypertext transfer protocol) request into an HttpRequest object
     * It reads the headers in a different way, it first checks if the header has ": " (a space after the colon), if not it uses the standard ":"
     *
     * @param request a raw HTTP request
     * @return an HttpRequest object
     * @throws HttpException if the request string are broken
     * @author Daniel Richard (Laivy)
     * @since 1.0-SNAPSHOT
     */
    @Experimental
    public static @NotNull HttpRequest parseRequest(final String request) throws HttpException {
        @NotNull String[] parts = request.replaceAll("\n", " ").split(" ");
        @NotNull HttpRequest httpRequest;

        try {
            // Method and uri
            @NotNull String method = parts[0];
            @NotNull String uri = parts[1];
            // Version
            @NotNull String version = parts[2];
            // Create request
            httpRequest = new BasicHttpRequest(method, uri, parseVersion(version));
        } catch (@NotNull Throwable throwable) {
            throw new HttpException("cannot read request basics", throwable);
        }

        try {
            // Headers
            @NotNull String headers = request.substring(Arrays.stream(parts).map(string -> string + " ").collect(Collectors.joining()).length());
            @NotNull Matcher matcher = HEADERS_SPLIT_PATTERN.matcher(headers);

            while (matcher.find()) {
                @NotNull String key = matcher.group(1);
                @NotNull String value = matcher.group(2);

                httpRequest.addHeader(key, value);
            }
        } catch (@NotNull Throwable throwable) {
            throw new HttpException("cannot read request headers", throwable);
        }

        return httpRequest;
    }

    @Experimental
    public static @NotNull HttpResponse parseResponse(final String response) throws HttpException {
        @NotNull String[] content = response.split("\n\n", 2);
        @NotNull String[] parts = content[0].replaceAll("\n", " ").split(" ");

        @NotNull HttpResponse httpResponse;

        try {
            // Status line
            @NotNull StatusLine line = new BasicStatusLine(parseVersion(parts[0]), Integer.parseInt(parts[1]), parts[2]);
            httpResponse = new BasicHttpResponse(line);
        } catch (@NotNull Throwable throwable) {
            throw new HttpException("cannot read response status line", throwable);
        }

        try {
            // Headers
            @NotNull String headers = content[0].substring(Arrays.stream(parts).limit(3).map(string -> string + " ").collect(Collectors.joining()).length());
            @NotNull Matcher matcher = HEADERS_SPLIT_PATTERN.matcher(headers);

            while (matcher.find()) {
                @NotNull String key = matcher.group(1);
                @NotNull String value = matcher.group(2);

                httpResponse.addHeader(key, value);
            }
        } catch (@NotNull Throwable throwable) {
            throw new HttpException("cannot read response headers", throwable);
        }

        try {
            // Content
            @Nullable ContentType contentType = null;
            if (httpResponse.containsHeader("Content-Type")) {
                contentType = ContentType.parse(httpResponse.getLastHeader("Content-Type").getValue());
            }

            // Body
            if (content.length > 1) {
                @NotNull String body = content[1];
                httpResponse.setEntity(new StringEntity(body, contentType));
            } else {
                httpResponse.setEntity(new BasicHttpEntity() {
                    {
                        this.contentType = httpResponse.getLastHeader("Content-Type");
                    }
                });
            }
        } catch (@NotNull Throwable throwable) {
            throw new HttpException("cannot read response body", throwable);
        }

        return httpResponse;
    }

    public static @NotNull ProtocolVersion parseVersion(@NotNull String version) {
        @NotNull String[] versionSplit = version.split("/");
        int major = Integer.parseInt(versionSplit[1].split("\\.")[0]);
        int minor = Integer.parseInt(versionSplit[1].split("\\.")[1]);

        return new ProtocolVersion(versionSplit[0], major, minor);
    }

}