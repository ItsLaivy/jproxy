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
     * @apiNote Last change: 23/02/2024 | 16:33 (GMT-3)
     * @apiNote This regex has some syntax issues that should be fixed later
     */
    @NotNull HeaderKey ACCEPT = HeaderKey.create("Accept", Pattern.compile("^([a-zA-Z0-9+-.*]+/[a-zA-Z0-9+-.*]+(; ?q=(0(\\.\\d{1,2})?|1(\\.0{1,2})?))?(, *)?)+$"));

    @NotNull HeaderKey ACCEPT_CH = HeaderKey.create("Accept-CH");
    @NotNull HeaderKey ACCEPT_CH_LIFETIME = HeaderKey.create("Accept-CH-Lifetime", Pattern.compile("^\\d+$"));

    /**
     * @see <a href="regexr.com/7sfpg">RegExr Tests</a>
     * @apiNote Last change: 23/02/2024 | 16:19 (GMT-3)
     * @apiNote This regex has some syntax issues that should be fixed later
     */
    @NotNull HeaderKey ACCEPT_CHARSET = HeaderKey.create("Accept-Charset", Pattern.compile("^([\\w\\-]+(; ?q=(0(\\.\\d{1,2})?|1(\\.0{1,2})?))?(, *)?)+$"));

    /**
     * @see <a href="regexr.com/7sfqn">RegExr Tests</a>
     * @apiNote Last change: 23/02/2024 | 16:17 (GMT-3)
     * @apiNote This regex has some syntax issues that should be fixed later
     */
    @NotNull HeaderKey ACCEPT_ENCODING = HeaderKey.create("Accept-Encoding", Pattern.compile("^([\\w\\-*]+(; ?q=(0(\\.\\d{1,2})?|1(\\.0{1,2})?))?(, *)?)+$"));

    /**
     * @see <a href="regexr.com/7sfs4">RegExr Tests</a>
     * @apiNote Last change: 23/02/2024 | 16:23 (GMT-3)
     * @apiNote This regex has some syntax issues that should be fixed later
     */
    @NotNull HeaderKey ACCEPT_LANGUAGE = HeaderKey.create("Accept-Language", Pattern.compile("^([\\w\\-*]+(; ?q=(0(\\.\\d{1,2})?|1(\\.0{1,2})?))?(, *)?)+$"));

    /**
     * @see <a href="regexr.com/7sfsd">RegExr Tests</a>
     * @apiNote Last change: 23/02/2024 | 16:30 (GMT-3)
     * @apiNote This regex has some syntax issues that should be fixed later
     */
    @NotNull HeaderKey ACCEPT_PATCH = HeaderKey.create("Accept-Patch", Pattern.compile("^([a-zA-Z0-9+-.*]+/[a-zA-Z0-9+-.*]+(; ?charset=([\\w\\-*]+?(, *)?))?(, *)?)+$"));

    /**
     * @see <a href="regexr.com/7sft5">RegExr Tests</a>
     * @apiNote Last change: 23/02/2024 | 16:36 (GMT-3)
     * @apiNote This regex has some syntax issues that should be fixed later
     */
    @NotNull HeaderKey ACCEPT_POST = HeaderKey.create("Accept-Post", Pattern.compile("^([a-zA-Z0-9+-.*]+/[a-zA-Z0-9+-.*]+(, *)?)+$"));

    @NotNull HeaderKey AGE = HeaderKey.create("Age", Pattern.compile("^\\d+$"));

    /**
     * @see <a href="regexr.com/7sftn">RegExr Tests</a>
     * @apiNote Last change: 23/02/2024 | 16:49 (GMT-3)
     */
    @NotNull HeaderKey Allow = HeaderKey.create("Allow", Pattern.compile("^(GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS)(, ?(GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS))*?$"));

    // fixme: Accept-Ranges Access-Control-Allow-Credentials Access-Control-Allow-Headers Access-Control-Allow-Methods Access-Control-Allow-Origin Access-Control-Expose-Headers Access-Control-Max-Age Access-Control-Request-Headers Access-Control-Request-Method

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
