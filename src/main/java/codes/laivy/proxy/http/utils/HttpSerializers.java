package codes.laivy.proxy.http.utils;

import org.apache.http.*;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class HttpSerializers {

    private static final @NotNull Pattern HEADERS_SPLIT_PATTERN = Pattern.compile("(\\S+?):\\s?(.*?)(?=\\s\\S+?:|$)");

    private HttpSerializers() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull Serializer<HttpRequest> getHttpRequest() {
        return new Serializer<HttpRequest>() {
            @Override
            public @NotNull ByteBuffer serialize(@UnknownNullability HttpRequest request) throws Exception {
                @NotNull RequestLine line = request.getRequestLine();
                @NotNull StringBuilder builder = new StringBuilder(line.getMethod() + " " + line.getUri() + " " + line.getProtocolVersion() + "\n");

                for (@NotNull Header header : request.getAllHeaders()) {
                    builder.append(header.getName()).append(": ").append(header.getValue()).append("\n");
                }

                builder.append("\n");

                return ByteBuffer.wrap(builder.toString().getBytes(StandardCharsets.UTF_8));
            }

            @Override
            public @UnknownNullability HttpRequest deserialize(@NotNull ByteBuffer buffer) throws Exception {
                @NotNull String request = new String(buffer.array(), StandardCharsets.UTF_8);
                @NotNull String[] parts = request.replaceAll("\r", "").replaceAll("\n", " ").split(" ");
                @NotNull HttpRequest httpRequest;

                try {
                    // Method, uri and protocol version
                    @NotNull String method = parts[0];
                    @NotNull String uri = parts[1];
                    @NotNull ProtocolVersion version = getProtocolVersion().deserialize(ByteBuffer.wrap(parts[2].getBytes(StandardCharsets.UTF_8)));

                    // Create request
                    httpRequest = new BasicHttpRequest(method, uri, version);
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
        };
    }
    public static @NotNull Serializer<HttpResponse> getHttpResponse() {
        return new Serializer<HttpResponse>() {
            @Override
            public @NotNull ByteBuffer serialize(@UnknownNullability HttpResponse response) throws HttpException {
                @NotNull StatusLine line = response.getStatusLine();
                @NotNull StringBuilder builder = new StringBuilder(line.getProtocolVersion() + " " + line.getStatusCode() + " " + line.getReasonPhrase() + "\n");

                try {
                    for (Header header : response.getAllHeaders()) {
                        builder.append(header.getName()).append(": ").append(header.getValue()).append("\n");
                    }
                } catch (@NotNull Throwable throwable) {
                    throw new HttpException("cannot serialize http response headers", throwable);
                }

                try {
                    builder.append("\n");
                    if (response.getEntity() != null) {
                        builder.append(new Scanner(response.getEntity().getContent()).useDelimiter("\\A").next());
                    }
                } catch (@NotNull Throwable throwable) {
                    throw new HttpException("cannot serialize http response content", throwable);
                }

                return ByteBuffer.wrap(builder.toString().getBytes(StandardCharsets.UTF_8));
            }

            @Override
            public @UnknownNullability HttpResponse deserialize(@NotNull ByteBuffer buffer) throws Exception {
                @NotNull String[] content = new String(buffer.array(), StandardCharsets.UTF_8).replaceAll("\r", "").split("\n\n", 2);
                @NotNull String[] parts = content[0].replaceAll("\n", " ").split(" ");

                @NotNull HttpResponse httpResponse;

                try {
                    // Status line
                    @NotNull ProtocolVersion version = getProtocolVersion().deserialize(ByteBuffer.wrap(parts[0].getBytes(StandardCharsets.UTF_8)));
                    @NotNull StatusLine line = new BasicStatusLine(version, Integer.parseInt(parts[1]), parts[2]);
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
        };
    }
    public static @NotNull Serializer<ProtocolVersion> getProtocolVersion() {
        return new Serializer<ProtocolVersion>() {
            @Override
            public @NotNull ByteBuffer serialize(@UnknownNullability ProtocolVersion object) {
                byte[] result = object.toString().getBytes(StandardCharsets.UTF_8);
                return ByteBuffer.wrap(result);
            }

            @Override
            public @UnknownNullability ProtocolVersion deserialize(@NotNull ByteBuffer buffer) {
                try {
                    @NotNull String string = new String(buffer.array());

                    @NotNull String[] parts = string.split("/");
                    @NotNull String[] version = parts[1].split("\\.");

                    return new ProtocolVersion(parts[0], Integer.parseInt(version[0]), Integer.parseInt(version[1]));
                } catch (@NotNull Throwable throwable) {
                    throw new IllegalArgumentException("cannot parse protocol version", throwable);
                }
            }
        };
    }

    // Classes

    public interface Serializer<T> {

        @NotNull ByteBuffer serialize(@UnknownNullability T object) throws Exception;
        @UnknownNullability T deserialize(@NotNull ByteBuffer buffer) throws Exception;

    }

}
