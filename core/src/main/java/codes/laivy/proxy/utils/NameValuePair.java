package codes.laivy.proxy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Objects;

public interface NameValuePair {

    @UnknownNullability String getName();
    @UnknownNullability String getValue();

    static @NotNull NameValuePair create(final @Nullable String name, final @Nullable String value) {
        return new NameValuePair() {
            @Override
            public @UnknownNullability String getName() {
                return name;
            }
            @Override
            public @UnknownNullability String getValue() {
                return value;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof NameValuePair && ((NameValuePair) obj).getName().equals(name);
            }
            @Override
            public int hashCode() {
                return Objects.hash(getName());
            }
        };
    }

}
