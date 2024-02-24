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

/**
 * This interface represents an HTTP request.
 *
 * @author Daniel Richard (Laivy)
 * @version 1.0-SNAPSHOT
 */
public interface HttpRequest {

    // Static initializers

    static @NotNull HttpRequest create(@NotNull HttpVersion version, @NotNull Method method, @Nullable URIAuthority authority, @NotNull URI uri, @NotNull Charset charset, @NotNull MutableHeaders headers, @Nullable Message message) {
        return new HttpRequestImpl(version, method, authority, uri, charset, headers, message);
    }

    // Object

    /**
     * Retrieves the raw bytes of this request, which is the purest form of the request data.
     * @return The raw bytes of the request
     */
    byte[] getBytes();

    @NotNull Method getMethod();

    /**
     * Retrieves the version of this HTTP request
     * @return the version of this request
     */
    @NotNull HttpVersion getVersion();

    /**
     * Retrieves the authority of the request, which can be null.
     * @return The authority of the request
     */
    @Nullable URIAuthority getAuthority();

    /**
     * Retrieves the URI path of this request.
     * @return The URI path of the request
     */
    @NotNull URI getUri();

    /**
     * Retrieves the charset of this request.
     * @return The charset of the request
     */
    @NotNull Charset getCharset();

    /**
     * Retrieves the headers of this request.
     * @return The headers of the request
     */
    @NotNull MutableHeaders getHeaders();

    /**
     * Retrieves the message, which is the body of the request. It can be null if there is no message.
     * @return The message body of the request
     */
    @Nullable Message getMessage();

}