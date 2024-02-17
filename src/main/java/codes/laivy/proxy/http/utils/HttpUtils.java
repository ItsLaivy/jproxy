package codes.laivy.proxy.http.utils;

import org.apache.http.*;
import org.apache.http.annotation.Experimental;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class HttpUtils {

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
    public static @NotNull HttpRequest parseRequest(String request) throws HttpException {
        @NotNull HttpRequest httpRequest;
        request = request.replaceAll("\n", "");

        try {
            // Request basics
            @NotNull String[] requestLine = request.split(" ");
            // Method and uri
            @NotNull String method = requestLine[0];
            @NotNull String uri = requestLine[1];
            // Version
            @NotNull String version = requestLine[2];
            // Create request
            httpRequest = new BasicHttpRequest(method, uri, parseVersion(version));
        } catch (@NotNull Throwable throwable) {
            throw new HttpException("cannot read request basics", throwable);
        }

        try {
            // Headers
            @NotNull String headers = request.substring(Arrays.stream(request.split(" ")).map(string -> string + " ").collect(Collectors.joining()).length());
            @NotNull Pattern pattern = Pattern.compile("(\\S+?):\\s?(.*?)(?=\\s\\S+?:|$)");
            @NotNull Matcher matcher = pattern.matcher(headers);

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