package codes.laivy.proxy.http.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

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
        };
    }

}
