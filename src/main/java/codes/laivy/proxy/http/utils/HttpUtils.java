package codes.laivy.proxy.http.utils;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.ProtocolVersion;
import org.apache.http.annotation.Experimental;
import org.apache.http.message.BasicHttpRequest;
import org.jetbrains.annotations.NotNull;

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

    public static @NotNull ProtocolVersion parseVersion(@NotNull String version) {
        @NotNull String[] versionSplit = version.split("/");
        int major = Integer.parseInt(versionSplit[1].split("\\.")[0]);
        int minor = Integer.parseInt(versionSplit[1].split("\\.")[1]);

        return new ProtocolVersion(versionSplit[0], major, minor);
    }

}