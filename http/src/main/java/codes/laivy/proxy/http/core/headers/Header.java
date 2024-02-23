package codes.laivy.proxy.http.core.headers;

import codes.laivy.proxy.http.core.NameValuePair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface Header extends NameValuePair {

    @NotNull HeaderKey getKey();

    @Override
    default @NotNull String getName() {
        return getKey().getName();
    }

    @Override
    @NotNull String getValue();

    static @NotNull Header create(final @NotNull HeaderKey key, final @NotNull String value) {
        return new Header() {
            @Override
            public @NotNull HeaderKey getKey() {
                return key;
            }

            @Override
            @Contract(pure = true)
            public @NotNull String getValue() {
                return value;
            }
        };
    }
    static @NotNull Header create(final @NotNull HeaderKey key, final @NotNull Object object) {
        return new Header() {
            @Override
            public @NotNull HeaderKey getKey() {
                return key;
            }

            @Override
            public @NotNull String getValue() {
                return object.toString();
            }
        };
    }

}
