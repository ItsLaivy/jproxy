package codes.laivy.proxy.utils;

import codes.laivy.proxy.exception.SerializationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.nio.ByteBuffer;

public interface Serializer<T> {

    @NotNull ByteBuffer serialize(@UnknownNullability T object) throws SerializationException;

    @UnknownNullability T deserialize(@NotNull ByteBuffer buffer) throws SerializationException;

}
