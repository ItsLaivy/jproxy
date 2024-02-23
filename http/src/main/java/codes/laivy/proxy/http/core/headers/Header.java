package codes.laivy.proxy.http.core.headers;

import codes.laivy.proxy.http.core.NameValuePair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface Header extends NameValuePair {

    @Override
    @NotNull String getName();

    @Override
    @NotNull String getValue();

    static @NotNull Header create(final @NotNull String name, final @NotNull String value) {
        return new Header() {
            @Override
            @Contract(pure = true)
            public @NotNull String getName() {
                return name;
            }

            @Override
            @Contract(pure = true)
            public @NotNull String getValue() {
                return value;
            }
        };
    }
    static @NotNull Header create(final @NotNull String name, final @NotNull Object object) {
        return new Header() {
            @Override
            @Contract(pure = true)
            public @NotNull String getName() {
                return name;
            }

            @Override
            public @NotNull String getValue() {
                return object.toString();
            }
        };
    }

}
