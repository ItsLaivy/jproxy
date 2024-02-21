package codes.laivy.proxy.http.utils;

import codes.laivy.proxy.exception.SerializationException;
import codes.laivy.proxy.http.core.SecureHttpRequest;
import codes.laivy.proxy.http.core.SecureHttpResponse;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class HttpSerializers {

    // Variables

    private static final @NotNull Pattern HEADERS_SPLIT_PATTERN = Pattern.compile("(\\S+?):\\s?(.*?)(?=\\s\\S+?:|$)");

    private static final @NotNull Serializer<HttpRequest> request = new Serializer<HttpRequest>() {
        @Override
        public @NotNull ByteBuffer serialize(@UnknownNullability HttpRequest request) throws SerializationException {
            try {
                if (request instanceof SecureHttpRequest) {
                    return ByteBuffer.wrap(((SecureHttpRequest) request).getData());
                }
            } catch (@NotNull Throwable throwable) {
                throw new SerializationException("cannot serialize secure http request", throwable);
            }

            try {
                @NotNull StringBuilder builder = new StringBuilder(request.getMethod() + " " + request.getUri() + " " + request.getVersion() + "\r\n");
                builder.append(new String(getHeaders().serialize(request).array(), StandardCharsets.UTF_8));
                builder.append("\r\n");

                if (request instanceof HttpEntityContainer) {
                    @Nullable ContentType contentType = HttpUtils.getContentType(request);
                    builder.append(HttpUtils.read((HttpEntityContainer) request, contentType != null ? contentType.getCharset() : null));
                }

                return ByteBuffer.wrap(builder.toString().getBytes(StandardCharsets.UTF_8));
            } catch (@NotNull Throwable throwable) {
                throw new SerializationException(throwable);
            }
        }
        @Override
        public @UnknownNullability HttpRequest deserialize(@NotNull ByteBuffer buffer) throws SerializationException {
            byte[] bytes = buffer.array();
            buffer.clear();

            // todo: 400: Bad Request - if the request doesn't have the "Host" parameter in HTTP/1.1 requests. And multiples connections with different hostings

            if (HttpUtils.isSecureData(bytes)) {
                return new SecureHttpRequest(bytes);
            }

            @NotNull String[] content = new String(bytes, StandardCharsets.UTF_8).replaceAll("\r", "").split("\n\n", 2);
            @NotNull String[] parts = content[0].replaceAll("\n", " ").split(" ");
            @NotNull MessageHeaders headers = getHeaders().deserialize(ByteBuffer.wrap(content[0].substring(Arrays.stream(parts).limit(3).map(string -> string + " ").collect(Collectors.joining()).length()).getBytes()));

            @NotNull HttpRequest httpRequest;

            try {
                // Method, uri and protocol version
                @NotNull String method = parts[0];
                @NotNull URI uri = new URI(parts[1]);
                @NotNull ProtocolVersion version = getProtocolVersion().deserialize(ByteBuffer.wrap(parts[2].getBytes(StandardCharsets.UTF_8)));

                // Create request
                if (content.length > 1) { // Request with body
                    @NotNull String body = content[1];

                    httpRequest = new BasicClassicHttpRequest(method, uri);
                    httpRequest.setVersion(version);

                    ((BasicClassicHttpRequest) httpRequest).setEntity(new StringEntity(body, HttpUtils.getContentType(headers)));
                } else { // Request without body
                    httpRequest = new BasicHttpRequest(method, uri);
                    httpRequest.setVersion(version);
                }

                // Add headers
                for (Header header : headers.getHeaders()) {
                    httpRequest.addHeader(header);
                }
            } catch (@NotNull Throwable throwable) {
                throw new SerializationException("cannot read request basics", throwable);
            }

            return httpRequest;
        }
    };

    private static final @NotNull Serializer<HttpResponse> response = new Serializer<HttpResponse>() {
        @Override
        public @NotNull ByteBuffer serialize(@UnknownNullability HttpResponse response) throws SerializationException {
            try {
                if (response instanceof SecureHttpResponse) {
                    return ByteBuffer.wrap(((SecureHttpResponse) response).getData());
                }
            } catch (@NotNull Throwable throwable) {
                throw new SerializationException("cannot serialize secure http response", throwable);
            }

            @NotNull StringBuilder builder = new StringBuilder((response.getVersion() != null ? response.getVersion() : HttpVersion.DEFAULT.format()) + " " + response.getCode() + " " + response.getReasonPhrase() + "\r\n");
            builder.append(new String(getHeaders().serialize(response).array(), StandardCharsets.UTF_8));
            builder.append("\r\n");

            try {
                if (response instanceof HttpEntityContainer) {
                    @Nullable ContentType contentType = HttpUtils.getContentType(response);
                    builder.append(HttpUtils.read((HttpEntityContainer) response, contentType != null ? contentType.getCharset() : null));
                }
            } catch (@NotNull Throwable throwable) {
                throw new SerializationException("cannot serialize http response content", throwable);
            }

            return ByteBuffer.wrap(builder.toString().getBytes(StandardCharsets.UTF_8));
        }
        @Override
        public @UnknownNullability HttpResponse deserialize(@NotNull ByteBuffer buffer) throws SerializationException {
            byte[] bytes = buffer.array();
            buffer.clear();

            if (HttpUtils.isSecureData(bytes)) {
                return new SecureHttpResponse(bytes);
            }

            @NotNull String[] content = new String(bytes, StandardCharsets.UTF_8).replaceAll("\r", "").split("\n\n", 2);
            @NotNull String[] parts = content[0].replaceAll("\n", " ").split(" ");
            @NotNull MessageHeaders headers = getHeaders().deserialize(ByteBuffer.wrap(content[0].substring(Arrays.stream(parts).limit(3).map(string -> string + " ").collect(Collectors.joining()).length()).getBytes()));

            @NotNull HttpResponse response;
            @NotNull StatusLine line;

            try {
                // Status line
                @NotNull ProtocolVersion version = getProtocolVersion().deserialize(ByteBuffer.wrap(parts[0].getBytes(StandardCharsets.UTF_8)));
                line = new StatusLine(version, Integer.parseInt(parts[1]), parts[2]);
            } catch (@NotNull Throwable throwable) {
                throw new SerializationException("cannot read response status line", throwable);
            }

            try {
                // Body
                if (content.length > 1) {
                    @NotNull String body = content[1];

                    response = new BasicClassicHttpResponse(line.getStatusCode(), line.getReasonPhrase());
                    ((BasicClassicHttpResponse) response).setEntity(new StringEntity(body, HttpUtils.getContentType(headers)));
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

    private static final @NotNull Serializer<ProtocolVersion> version = new Serializer<ProtocolVersion>() {
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

    private static final @NotNull Serializer<MessageHeaders> headers = new Serializer<MessageHeaders>() {
        @Override
        public @NotNull ByteBuffer serialize(@UnknownNullability MessageHeaders object) {
            @NotNull StringBuilder builder = new StringBuilder();

            // todo: test multiples headers with the same name
            for (@NotNull Header header : object.getHeaders()) {
                builder.append(header.getName()).append(": ").append(header.getValue()).append("\r\n");
            }

            return ByteBuffer.wrap(builder.toString().getBytes(StandardCharsets.UTF_8));
        }
        @Override
        public @UnknownNullability MessageHeaders deserialize(@NotNull ByteBuffer buffer) {
            @NotNull String string = new String(buffer.array(), StandardCharsets.UTF_8).replaceAll("\r", "").replaceAll("\n", " ");
            @NotNull Pattern pattern = Pattern.compile("([^:\\s]+):\\s*(.+?)(?=(\\s+[^:\\s]+:|$))");
            @NotNull Matcher matcher = pattern.matcher(string);

            @NotNull HeaderGroup group = new HeaderGroup();

            while (matcher.find()) {
                @NotNull Header header = new BasicHeader(matcher.group(1), matcher.group(2), HttpUtils.isHeaderSensitive(matcher.group(1)));
                group.addHeader(header);
            }

            return group;
        }
    };

    // Object

    private HttpSerializers() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull Serializer<HttpRequest> getHttpRequest() {
        return request;
    }
    public static @NotNull Serializer<HttpResponse> getHttpResponse() {
        return response;
    }
    public static @NotNull Serializer<ProtocolVersion> getProtocolVersion() {
        return version;
    }
    public static @NotNull Serializer<MessageHeaders> getHeaders() {
        return headers;
    }

    // Classes

}
