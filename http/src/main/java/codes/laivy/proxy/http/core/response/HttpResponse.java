package codes.laivy.proxy.http.core.response;

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

    /**
     * Retrieves the raw bytes of this response, which is the purest form of the response data.
     * @return The raw bytes of the response
     */
    byte[] getBytes();

    /**
     * Retrieves the version of this HTTP request
     * @return the version of this request
     */
    @NotNull HttpVersion getVersion();

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