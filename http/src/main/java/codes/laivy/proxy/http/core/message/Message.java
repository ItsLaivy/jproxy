package codes.laivy.proxy.http.core.message;

import org.jetbrains.annotations.NotNull;

import java.io.*;

public interface Message extends Closeable {

    @NotNull InputStream getContent() throws IOException;

    void writeTo(@NotNull OutputStream stream) throws IOException;

    long size();

    static @NotNull StringMessage create(@NotNull String message) {
        byte[] bytes = message.getBytes();
        return new StringMessage(bytes);
    }

}
