package codes.laivy.proxy.http.core.message;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public final class StringMessage implements Message {

    private final byte @NotNull [] bytes;

    public StringMessage(@NotNull String message) {
        this.bytes = message.getBytes();
    }
    public StringMessage(byte @NotNull [] bytes) {
        this.bytes = bytes;
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
