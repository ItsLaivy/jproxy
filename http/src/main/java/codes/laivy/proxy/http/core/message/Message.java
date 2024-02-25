package codes.laivy.proxy.http.core.message;

import org.jetbrains.annotations.NotNull;

import java.io.Closeable;

public interface Message extends Closeable {

    byte[] getContent();

    long size();

    static @NotNull StringMessage create(@NotNull String message) {
        byte[] bytes = message.getBytes();
        return new StringMessage(bytes);
    }

}
