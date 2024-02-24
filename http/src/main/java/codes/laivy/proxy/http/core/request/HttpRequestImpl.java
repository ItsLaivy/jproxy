package codes.laivy.proxy.http.core.request;

import codes.laivy.proxy.http.core.Method;
import codes.laivy.proxy.http.core.URIAuthority;
import codes.laivy.proxy.http.core.headers.Headers.MutableHeaders;
import codes.laivy.proxy.http.core.message.Message;
import codes.laivy.proxy.http.core.protocol.HttpVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.nio.charset.Charset;

final class HttpRequestImpl implements HttpRequest {

    private final @NotNull HttpVersion version;
    private final @NotNull Method method;
    private final @Nullable URIAuthority authority;
    private final @NotNull URI uri;
    private final @NotNull Charset charset;
    private final @NotNull MutableHeaders headers;
    private final @Nullable Message message;

    public HttpRequestImpl(@NotNull HttpVersion version, @NotNull Method method, @Nullable URIAuthority authority, @NotNull URI uri, @NotNull Charset charset, @NotNull MutableHeaders headers, @Nullable Message message) {
        this.version = version;
        this.method = method;
        this.authority = authority;
        this.uri = uri;
        this.charset = charset;
        this.headers = headers;
        this.message = message;
    }

    @Override
    public byte[] getBytes() {
        return getVersion().getFactory().getRequest().wrap(this);
    }

    @Override
    public @NotNull HttpVersion getVersion() {
        return version;
    }

    @Override
    public @NotNull Method getMethod() {
        return method;
    }

    @Override
    public @Nullable URIAuthority getAuthority() {
        return authority;
    }

    @Override
    public @NotNull URI getUri() {
        return uri;
    }

    @Override
    public @NotNull Charset getCharset() {
        return charset;
    }

    @Override
    public @NotNull MutableHeaders getHeaders() {
        return headers;
    }

    @Override
    public @Nullable Message getMessage() {
        return message;
    }
}
