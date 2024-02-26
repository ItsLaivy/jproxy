package codes.laivy.proxy.http.core.response;

import codes.laivy.proxy.http.core.HttpStatus;
import codes.laivy.proxy.http.core.headers.Headers.MutableHeaders;
import codes.laivy.proxy.http.core.message.Message;
import codes.laivy.proxy.http.core.protocol.HttpVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class HttpResponseImpl implements HttpResponse {

    private final @NotNull HttpStatus status;
    private final @NotNull HttpVersion version;
    private final @NotNull MutableHeaders headers;
    private final @Nullable Message message;

    public HttpResponseImpl(@NotNull HttpStatus status, @NotNull HttpVersion version, @NotNull MutableHeaders headers, @Nullable Message message) {
        this.status = status;
        this.version = version;
        this.headers = headers;
        this.message = message;
    }

    @Override
    public byte[] getBytes() {
        return getVersion().getFactory().getResponse().wrap(this);
    }

    @Override
    public @NotNull HttpStatus getStatus() {
        return status;
    }

    @Override
    public @NotNull HttpVersion getVersion() {
        return version;
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
