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
        return create(key, value, true);
    }
    static @NotNull Header create(final @NotNull HeaderKey key, final @NotNull String value, boolean unsafe) {
        if (unsafe && key.getPattern() != null && key.getPattern().matcher(value).matches()) {
            throw new IllegalArgumentException("the value '" + value + "' cannot be applied to header '" + key.getName() + "'. The pattern is '" + key.getPattern().pattern() + "'");
        }

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

    /**
     * @deprecated cannot check value using the key pattern
     */
    @Deprecated
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
