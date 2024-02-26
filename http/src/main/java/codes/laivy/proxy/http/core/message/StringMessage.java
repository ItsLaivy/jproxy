package codes.laivy.proxy.http.core.message;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.Charset;

public final class StringMessage implements Message {

    private final @NotNull Charset charset;
    private final byte @NotNull [] bytes;

    public StringMessage(@NotNull String message, @NotNull Charset charset) {
        this(message.getBytes(charset), charset);
    }
    public StringMessage(byte @NotNull [] bytes, @NotNull Charset charset) {
        this.bytes = bytes;
        this.charset = charset;
    }

    @Override
    public @NotNull Charset getCharset() {
        return charset;
    }

    @Override
    public byte[] getContent() {
        return bytes;
    }

    @Override
    public long size() {
        return bytes.length;
    }

    @Override
    public void close() throws IOException {

    }
}
