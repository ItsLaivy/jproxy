package codes.laivy.proxy.exception;

import codes.laivy.proxy.utils.Serializer;
import org.jetbrains.annotations.Nullable;

/**
 * Thrown when a problem occurs during the serialization or deserialization of an object by a {@link Serializer}.
 * @see Serializer
 */
public final class SerializationException extends Exception {

    public SerializationException() {
    }
    public SerializationException(@Nullable String message) {
        super(message);
    }
    public SerializationException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }
    public SerializationException(@Nullable Throwable cause) {
        super(cause);
    }
    public SerializationException(@Nullable String message, @Nullable Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
