package codes.laivy.proxy.http.exception;

import org.jetbrains.annotations.NotNull;

public final class UnsupportedHttpVersionException extends Exception {
    public UnsupportedHttpVersionException(@NotNull String message) {
        super(message);
    }
}
