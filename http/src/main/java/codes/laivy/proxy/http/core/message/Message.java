package codes.laivy.proxy.http.core.message;

import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.nio.charset.Charset;

public interface Message extends Closeable {

    @NotNull Charset getCharset();

    byte[] getContent();

    long size();

    static @NotNull StringMessage create(@NotNull String message, @NotNull Charset charset) {
        byte[] bytes = message.getBytes();
        return new StringMessage(bytes, charset);
    }

}
