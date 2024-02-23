package codes.laivy.proxy.http.core.headers;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;

public final class HeaderKey {

    // Static initializers

    public static final @NotNull Pattern NAME_FORMAT_REGEX = Pattern.compile("^[A-Za-z][A-Za-z0-9-]*$");

    public static @NotNull HeaderKey create(@NotNull String name) {
        return new HeaderKey(name, null);
    }
    public static @NotNull HeaderKey create(@NotNull String name, @NotNull Pattern pattern) {
        return new HeaderKey(name, pattern);
    }

    // Provided

    /**
     * @see <a href="regexr.com/7sfol">RegExr Tests</a>
     * @apiNote Last change: 23/02/2024 | 15:47 (GMT-3)
     */
    @NotNull HeaderKey ACCEPT = HeaderKey.create("Accept", Pattern.compile("^([a-zA-Z0-9+.*]+/[a-zA-Z0-9+.*]+(; ?q=[0-1](\\.[0-9]{1,2})?)?(, *)?)+$"));

    @NotNull HeaderKey ACCEPT_CH = HeaderKey.create("Accept-CH");
    @NotNull HeaderKey ACCEPT_CH_LIFETIME = HeaderKey.create("Accept-CH-Lifetime", Pattern.compile("^\\d+$"));

    /**
     * @see <a href="regexr.com/7sfpg">RegExr Tests</a>
     * @apiNote Last change: 23/02/2024 | 15:58 (GMT-3)
     */
    @NotNull HeaderKey ACCEPT_CHARSET = HeaderKey.create("Accept-Charset", Pattern.compile("^([\\w\\-]+(; ?q=[0-1]\\.?[0-9](\\.[0-9]{1,2})?)?(, *)?)+$"));
    
    // Object

    private final @NotNull String name;
    private final @Nullable Pattern pattern;

    private HeaderKey(@NotNull String name, @Nullable Pattern pattern) {
        this.name = name;
        this.pattern = pattern;

        if (!name.matches(NAME_FORMAT_REGEX.pattern())) {
            throw new IllegalStateException("this name '" + name + "' doesn't follows the header name format regex");
        }
    }

    @Contract(pure = true)
    public @NotNull String getName() {
        return name;
    }
    @Contract(pure = true)
    public @Nullable Pattern getPattern() {
        return pattern;
    }

    // Implementations

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof HeaderKey)) return false;
        HeaderKey headerKey = (HeaderKey) object;
        return getName().equalsIgnoreCase(headerKey.getName());
    }
    @Override
    public int hashCode() {
        return Objects.hash(getName().toLowerCase());
    }
    @Override
    public @NotNull String toString() {
        return getName();
    }

}
