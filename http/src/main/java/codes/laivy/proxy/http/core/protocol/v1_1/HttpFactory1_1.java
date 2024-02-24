package codes.laivy.proxy.http.core.protocol.v1_1;

import codes.laivy.proxy.http.connection.HttpProxyClient;
import codes.laivy.proxy.http.core.ContentType;
import codes.laivy.proxy.http.core.HttpStatus;
import codes.laivy.proxy.http.core.Method;
import codes.laivy.proxy.http.core.URIAuthority;
import codes.laivy.proxy.http.core.headers.Header;
import codes.laivy.proxy.http.core.headers.HeaderKey;
import codes.laivy.proxy.http.core.headers.Headers.MutableHeaders;
import codes.laivy.proxy.http.core.message.Message;
import codes.laivy.proxy.http.core.message.StringMessage;
import codes.laivy.proxy.http.core.protocol.HttpFactory;
import codes.laivy.proxy.http.core.protocol.HttpVersion;
import codes.laivy.proxy.http.core.request.HttpRequest;
import codes.laivy.proxy.http.core.response.HttpResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class HttpFactory1_1 implements HttpFactory {

    // Parsers

    private final @NotNull Request request = new Request() {
        @Override
        public @NotNull HttpRequest parse(@NotNull HttpProxyClient client, byte[] data) throws ParseException {
            @NotNull String string = new String(data, StandardCharsets.UTF_8);

            if (!isCompatible(client, string)) {
                throw new ParseException("not a valid " + getVersion() + " response", -1);
            }

            // Content
            @NotNull String[] content = string.split("\n\n\r\n", 2);
            // Request line
            @NotNull String requestLine = content[0].split("\r\n", 2)[0];
            @NotNull Matcher matcher = Pattern.compile("\\S+").matcher(requestLine);

            @NotNull Method method;
            @Nullable URIAuthority authority;
            @NotNull URI uri;

            try {
                method = Method.valueOf(matcher.group(0).toUpperCase());
            } catch (@NotNull IllegalArgumentException e) {
                throw new ParseException("cannot parse '" + matcher.group(0) + "' as a valid " + getVersion() + " request method", matcher.start(0));
            }

            try {
                @NotNull String uriString = matcher.group(1);

                if (URIAuthority.isUriAuthority(uriString)) {
                    authority = URIAuthority.parse(uriString);
                    uri = new URI(uriString.substring(authority.toString().length()));
                } else {
                    authority = null;
                    uri = new URI(uriString);
                }
            } catch (UnknownHostException e) {
                throw new ParseException("cannot parse '" + matcher.group(1) + "' as a valid host", matcher.start(1));
            } catch (URISyntaxException e) {
                throw new ParseException("cannot parse '" + matcher.group(1) + "' as a valid http uri", matcher.start(1));
            }

            // Retrieve headers
            @NotNull MutableHeaders headerList = codes.laivy.proxy.http.core.headers.Headers.createMutable();
            @NotNull String[] headerSection = content[0].substring(requestLine.length() + 4).split("\r\n");

            for (@NotNull String header : headerSection) {
                try {
                    headerList.add(getHeaders().parse(header.getBytes()));
                } catch (@NotNull ParseException exception) {
                    throw new ParseException("couldn't parse header '" + header + "': " + exception.getMessage(), 0);
                }
            }

            // Validate host header
            int count = headerList.count(HeaderKey.HOST);
            if (count == 0) {
                throw new ParseException("missing '" + HeaderKey.HOST + "' header", 0);
            } else if (count > 1) {
                throw new ParseException("multiples '" + HeaderKey.HOST + "' headers", 0);
            }
            // Charset
            @NotNull Charset charset = StandardCharsets.UTF_8;

            @NotNull Optional<Header> optional = headerList.first(HeaderKey.CONTENT_TYPE);
            if (optional.isPresent()) {
                @NotNull ContentType type = ContentType.parse(optional.get().getValue());
                charset = type.getCharset() != null ? type.getCharset() : StandardCharsets.UTF_8;
            }
            // Message
            @Nullable Message message = null;
            if (content.length == 2) {
                message = new StringMessage(content[1]);
            }

            return HttpRequest.create(getVersion(), method, authority, uri, charset, headerList, message);
        }

        @Override
        public byte[] wrap(@NotNull HttpRequest request) {
            if (!request.getHeaders().contains(HeaderKey.HOST)) {
                throw new IllegalStateException("the http requests from version " + getVersion() + " must have the '" + HeaderKey.HOST + "' header");
            } else if (request.getHeaders().count(HeaderKey.HOST) > 1) {
                throw new IllegalStateException("the http requests from version " + getVersion() + " cannot have multiples '" + HeaderKey.HOST + "' headers");
            }

            @NotNull StringBuilder builder = new StringBuilder();

            // Write request line
            @NotNull String authority = request.getAuthority() != null ? request.getAuthority().toString() : request.getUri().toString();
            builder.append(request.getMethod().name()).append(" ").append(authority).append(" ").append(getVersion()).append("\r\n");
            // Write headers
            for (@NotNull Header header : request.getHeaders()) {
                builder.append(new String(getHeaders().wrap(header))).append("\r\n");
            }
            // End request configurations
            builder.append("\r\n");
            // Write message if exists
            if (request.getMessage() != null) {
                @NotNull Message message = request.getMessage();
                builder.append(new String(message.getContent(), request.getCharset()));
            }

            return builder.toString().getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public boolean isCompatible(@NotNull HttpProxyClient client, byte[] data) {
            return this.isCompatible(client, new String(data));
        }
        public boolean isCompatible(@NotNull HttpProxyClient client, @NotNull String string) {
            if (!string.contains("\r\n") || !string.contains("\n\r\n")) {
                return false;
            }

            @NotNull String[] split = string.split("\r");
            return split[0].toUpperCase().endsWith(getVersion().toString()) && split[0].split(" ").length == 3;
        }
    };
    private final @NotNull Response response = new Response() {
        @Override
        public @NotNull HttpResponse parse(@NotNull HttpProxyClient client, byte[] data) throws ParseException {
            @NotNull String string = new String(data, StandardCharsets.UTF_8);

            if (!isCompatible(client, string)) {
                throw new ParseException("not a valid " + getVersion() + " response", -1);
            }

            @NotNull HttpStatus status;

            // Content
            @NotNull String[] content = string.split("\n\n\r\n", 2);
            // Request line
            @NotNull String[] responseLine = content[0].split("\r\n", 2)[0].split(" ", 3);

            try {
                int code = Integer.parseInt(responseLine[1]);
                @NotNull String message = responseLine[2];

                status = new HttpStatus(code, message);
            } catch (@NotNull Throwable throwable) {
                throw new ParseException("cannot parse response line: " + throwable.getMessage(), 0);
            }

            // Retrieve headers
            @NotNull MutableHeaders headerList = codes.laivy.proxy.http.core.headers.Headers.createMutable();
            @NotNull String[] headerSection = content[0].substring(content[0].split("\r\n", 2)[0].length() + 4).split("\r\n");

            for (@NotNull String header : headerSection) {
                try {
                    headerList.add(getHeaders().parse(header.getBytes()));
                } catch (@NotNull ParseException exception) {
                    throw new ParseException("couldn't parse header '" + header + "': " + exception.getMessage(), 0);
                }
            }
            // Charset
            @NotNull Charset charset = StandardCharsets.UTF_8;

            @NotNull Optional<Header> optional = headerList.first(HeaderKey.CONTENT_TYPE);
            if (optional.isPresent()) {
                @NotNull ContentType type = ContentType.parse(optional.get().getValue());
                charset = type.getCharset() != null ? type.getCharset() : StandardCharsets.UTF_8;
            }
            // Message
            @Nullable Message message = null;
            if (content.length == 2) {
                message = new StringMessage(content[1]);
            }

            return HttpResponse.create(status, getVersion(), charset, headerList, message);
        }

        @Override
        public byte[] wrap(@NotNull HttpResponse response) {
            @NotNull StringBuilder builder = new StringBuilder();
            builder.append(getVersion()).append(" ").append(response.getStatus().getCode()).append(response.getStatus().getMessage()).append("\r\n");
            // Write headers
            for (@NotNull Header header : response.getHeaders()) {
                builder.append(new String(getHeaders().wrap(header))).append("\r\n");
            }
            // End request configurations
            builder.append("\r\n");
            // Write message if exists
            if (response.getMessage() != null) {
                @NotNull Message message = response.getMessage();
                builder.append(new String(message.getContent(), response.getCharset()));
            }

            return builder.toString().getBytes();
        }

        @Override
        public boolean isCompatible(@NotNull HttpProxyClient client, byte[] data) {
            return isCompatible(client, new String(data));
        }
        public boolean isCompatible(@NotNull HttpProxyClient client, @NotNull String string) {
            if (!string.contains("\r\n") || !string.contains("\n\r\n")) {
                return false;
            }

            return string.startsWith(getVersion().toString()) && string.split(" ", 3).length == 3;
        }
    };
    private final @NotNull Headers headers = new Headers() {
        @Override
        public @NotNull Header parse(byte[] data) throws ParseException {
            @NotNull String string = new String(data);

            if (!isCompatible(data)) {
                throw new ParseException("not a valid " + getVersion() + " header: " + string, -1);
            }

            @NotNull String[] parts = string.split(":\\s*");
            @NotNull String name = parts[0];
            @NotNull String value = parts[1];

            @NotNull HeaderKey key = HeaderKey.create(name);
            return Header.create(key, value);
        }

        @Override
        public byte[] wrap(@NotNull Header header) {
            return (header.getName() + ": " + header.getValue()).getBytes();
        }

        @Override
        public boolean isCompatible(byte[] data) {
            return isCompatible(new String(data));
        }
        public boolean isCompatible(@NotNull String string) {
            return Pattern.compile(":\\s*").matcher(string).find();
        }
    };

    // Object

    private final @NotNull HttpVersion version;

    public HttpFactory1_1(@NotNull HttpVersion version) {
        this.version = version;
    }

    public @NotNull HttpVersion getVersion() {
        return version;
    }

    // Parsers

    @Override
    public @NotNull Request getRequest() {
        return request;
    }

    @Override
    public @NotNull Response getResponse() {
        return response;
    }

    @Override
    public @NotNull Headers getHeaders() {
        return headers;
    }
}
