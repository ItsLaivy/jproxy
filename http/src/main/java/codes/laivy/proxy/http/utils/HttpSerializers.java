package codes.laivy.proxy.http.utils;

import codes.laivy.proxy.exception.SerializationException;
import codes.laivy.proxy.utils.Serializer;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.net.URI;
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
            public @NotNull ByteBuffer serialize(@UnknownNullability HttpRequest request) throws SerializationException {
                try {
                    @NotNull StringBuilder builder = new StringBuilder(request.getMethod() + " " + request.getUri() + " " + request.getVersion() + "\r\n");

                    for (@NotNull Header header : request.getHeaders()) {
                        builder.append(header.getName()).append(": ").append(header.getValue()).append("\r\n");
                    }

                    builder.append("\r\n");

                    return ByteBuffer.wrap(builder.toString().getBytes(StandardCharsets.UTF_8));
                } catch (@NotNull Throwable throwable) {
                    throw new SerializationException(throwable);
                }
            }

            @Override
            public @UnknownNullability HttpRequest deserialize(@NotNull ByteBuffer buffer) throws SerializationException {
                @NotNull String request = new String(buffer.array(), StandardCharsets.UTF_8);
                @NotNull String[] parts = request.replaceAll("\r", "").replaceAll("\n", " ").split(" ");
                @NotNull HttpRequest httpRequest;

                try {
                    // Method, uri and protocol version
                    @NotNull String method = parts[0];
                    @NotNull URI uri = new URI(parts[1]);
                    @NotNull ProtocolVersion version = getProtocolVersion().deserialize(ByteBuffer.wrap(parts[2].getBytes(StandardCharsets.UTF_8)));

                    // Create request
                    httpRequest = new BasicHttpRequest(method, uri);
                    httpRequest.setVersion(version);
                } catch (@NotNull Throwable throwable) {
                    throw new SerializationException("cannot read request basics", throwable);
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
                    throw new SerializationException("cannot read request headers", throwable);
                }

                return httpRequest;
            }
        };
    }
    public static @NotNull Serializer<HttpResponse> getHttpResponse() {
        return new Serializer<HttpResponse>() {
            @Override
            public @NotNull ByteBuffer serialize(@UnknownNullability HttpResponse response) throws SerializationException {
                if (response.getVersion() == null) {
                    throw new NullPointerException("response version cannot be null");
                }

                @NotNull StringBuilder builder = new StringBuilder(response.getVersion() + " " + response.getCode() + " " + response.getReasonPhrase() + "\r\n");

                try {
                    for (Header header : response.getHeaders()) {
                        builder.append(header.getName()).append(": ").append(header.getValue()).append("\r\n");
                    }
                } catch (@NotNull Throwable throwable) {
                    throw new SerializationException("cannot serialize http response headers", throwable);
                }

                try {
                    builder.append("\r\n");

                    if (response instanceof HttpEntityContainer) {
                        builder.append(new Scanner(((HttpEntityContainer) response).getEntity().getContent()).useDelimiter("\\A").next());
                    }
                } catch (@NotNull Throwable throwable) {
                    throw new SerializationException("cannot serialize http response content", throwable);
                }

                return ByteBuffer.wrap(builder.toString().getBytes(StandardCharsets.UTF_8));
            }

            @Override
            public @UnknownNullability HttpResponse deserialize(@NotNull ByteBuffer buffer) throws SerializationException {
                @NotNull String[] content = new String(buffer.array(), StandardCharsets.UTF_8).replaceAll("\r", "").split("\n\n", 2);
                @NotNull String[] parts = content[0].replaceAll("\n", " ").split(" ");

                @NotNull HttpResponse response;
                @NotNull StatusLine line;
                @NotNull HeaderGroup headers = new HeaderGroup();

                try {
                    // Status line
                    @NotNull ProtocolVersion version = getProtocolVersion().deserialize(ByteBuffer.wrap(parts[0].getBytes(StandardCharsets.UTF_8)));
                    line = new StatusLine(version, Integer.parseInt(parts[1]), parts[2]);
                } catch (@NotNull Throwable throwable) {
                    throw new SerializationException("cannot read response status line", throwable);
                }

                try {
                    // Headers
                    @NotNull String headerString = content[0].substring(Arrays.stream(parts).limit(3).map(string -> string + " ").collect(Collectors.joining()).length());
                    @NotNull Matcher matcher = HEADERS_SPLIT_PATTERN.matcher(headerString);

                    while (matcher.find()) {
                        @NotNull String key = matcher.group(1);
                        @NotNull String value = matcher.group(2);

                        headers.addHeader(new BasicHeader(key, value));
                    }
                } catch (@NotNull Throwable throwable) {
                    throw new SerializationException("cannot read response headers", throwable);
                }

                try {
                    // Content
                    @Nullable ContentType contentType = null;
                    if (headers.containsHeader("Content-Type")) {
                        contentType = ContentType.parse(headers.getLastHeader("Content-Type").getValue());
                    }

                    // Body
                    if (content.length > 1) {
                        @NotNull String body = content[1];

                        response = new BasicClassicHttpResponse(line.getStatusCode(), line.getReasonPhrase());
                        ((BasicClassicHttpResponse) response).setEntity(new StringEntity(body, contentType));
                    } else {
                        response = new BasicHttpResponse(line.getStatusCode(), line.getReasonPhrase());
                    }

                    for (@NotNull Header header : headers.getHeaders()) {
                        response.addHeader(header);
                    }

                    response.setVersion(line.getProtocolVersion());
                } catch (@NotNull Throwable throwable) {
                    throw new SerializationException("cannot read response body", throwable);
                }

                return response;
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

}
