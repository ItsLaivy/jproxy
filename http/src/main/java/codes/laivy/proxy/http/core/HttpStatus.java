package codes.laivy.proxy.http.core;

import codes.laivy.proxy.http.core.headers.HeaderKey;
import codes.laivy.proxy.http.core.headers.Headers;
import codes.laivy.proxy.http.core.message.Message;
import codes.laivy.proxy.http.core.protocol.HttpVersion;
import codes.laivy.proxy.http.core.response.HttpResponse;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public class HttpStatus {

    // Static initializers

    public static final @NotNull HttpStatus CONTINUE = new HttpStatus(100, "Continue");
    public static final @NotNull HttpStatus SWITCHING_PROTOCOLS = new HttpStatus(101, "Switching Protocols", HeaderKey.UPGRADE);

    public static final @NotNull HttpStatus BAD_REQUEST = new HttpStatus(400, "Bad Request");

    // Object

    private final int code;
    private final @NotNull String message;

    private final @NotNull HeaderKey @NotNull [] headers;

    public HttpStatus(int code, @NotNull String message) {
        this(code, message, new HeaderKey[0]);
    }
    public HttpStatus(int code, @NotNull String message, @NotNull HeaderKey @NotNull ... headers) {
        this.code = code;
        this.message = message;
        this.headers = Arrays.copyOf(headers, headers.length);

        if (message.length() > 8192) {
            throw new IllegalStateException("http status message too large");
        }
    }

    // Getters

    @Contract(pure = true)
    public final int getCode() {
        return code;
    }

    @Contract(pure = true)
    public final @NotNull String getMessage() {
        return message;
    }

    /**
     * Retrieves the list of headers required for this HTTP status.
     * If a response has this status and does not contain the headers in this array,
     * a {@link NullPointerException} will be thrown during response serialization.
     *
     * @return The list of headers required for this HTTP status
     */
    @Contract(pure = true)
    public final @NotNull HeaderKey @NotNull [] getHeaders() {
        return headers;
    }

    @Contract(pure = true)
    public final @NotNull Category getCategory() {
        return Category.getCategory(this);
    }

    public final @NotNull HttpResponse createResponse(@NotNull HttpVersion version) {
        return createResponse(version, StandardCharsets.UTF_8, null);
    }
    public final @NotNull HttpResponse createResponse(@NotNull HttpVersion version, @NotNull Charset charset, @Nullable Message message) {
        return HttpResponse.create(this, version, charset, Headers.createMutable(), message);
    }

    // Implementations

    @Override
    public @NotNull String toString() {
        return String.valueOf(getCode());
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (this == object) return true;
        if (!(object instanceof HttpStatus)) return false;
        HttpStatus that = (HttpStatus) object;
        return getCode() == that.getCode();
    }
    @Override
    public int hashCode() {
        return Objects.hash(getCode());
    }

    // Classes

    public enum Category {
        INFORMATIONAL(100, 199),
        SUCCESSFUL(200, 299),
        REDIRECTION(300, 399),
        CLIENT_ERROR(400, 499),
        SERVER_ERROR(500, 599),
        ;

        private final int minimum;
        private final int maximum;

        Category(int minimum, int maximum) {
            this.minimum = minimum;
            this.maximum = maximum;
        }

        public int getMinimum() {
            return minimum;
        }
        public int getMaximum() {
            return maximum;
        }

        public static @NotNull Category getCategory(@NotNull HttpStatus status) throws IndexOutOfBoundsException {
            for (@NotNull Category category : values()) {
                if (status.getCode() >= category.getMinimum() && status.getCode() <= category.getMaximum()) {
                    return category;
                }
            }
            throw new IndexOutOfBoundsException("couldn't find a category for status code '" + status.getCode() + "'");
        }

    }

}
