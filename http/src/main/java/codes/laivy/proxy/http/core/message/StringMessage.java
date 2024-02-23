package codes.laivy.proxy.http.core.message;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class StringMessage implements Message {

    private final byte @NotNull [] bytes;

    public StringMessage(@NotNull String message) {
        this.bytes = message.getBytes();
    }
    public StringMessage(byte @NotNull [] bytes) {
        this.bytes = bytes;
    }

    @Override
    public @NotNull InputStream getContent() {
        return new ByteArrayInputStream(bytes);
    }

    @Override
    public void writeTo(final @NotNull OutputStream stream) throws IOException {
        stream.write(bytes);
        stream.flush();
    }

    @Override
    public long size() {
        return bytes.length;
    }

    @Override
    public void close() throws IOException {

    }
}
