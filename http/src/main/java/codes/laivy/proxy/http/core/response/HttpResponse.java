package codes.laivy.proxy.http.core.response;

import codes.laivy.proxy.http.core.HttpStatus;
import codes.laivy.proxy.http.core.message.Message;
import codes.laivy.proxy.http.core.protocol.HttpVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;

import static codes.laivy.proxy.http.core.headers.Headers.MutableHeaders;

/**
 * This interface represents an HTTP response.
 *
 * @author Daniel Richard (Laivy)
 * @version 1.0-SNAPSHOT
 */
public interface HttpResponse {

    // Static initializers

    static @NotNull HttpResponse create(@NotNull HttpStatus status, @NotNull HttpVersion version, @NotNull Charset charset, @NotNull MutableHeaders headers, @Nullable Message message) {
        return new HttpResponseImpl(status, version, charset, headers, message);
    }

    // Object

    /**
     * Retrieves the raw bytes of this response, which is the purest form of the response data.
     * @return The raw bytes of the response
     */
    byte[] getBytes();

    /**
     * Retrieves the status of this HTTP response
     * @return the version of this response
     */
    @NotNull HttpStatus getStatus();

    /**
     * Retrieves the version of this HTTP response
     * @return the version of this response
     */
    @NotNull HttpVersion getVersion();

    /**
     * Retrieves the charset of this response.
     * @return The charset of the response
     */
    // todo: this needs to be on Message class (maybe?)
    @NotNull Charset getCharset();

    /**
     * Retrieves the headers of this response.
     * @return The headers of the response
     */
    @NotNull MutableHeaders getHeaders();

    /**
     * Retrieves the message, which is the body of the response. It can be null if there is no message.
     * @return The message body of the response
     */
    @Nullable Message getMessage();

}