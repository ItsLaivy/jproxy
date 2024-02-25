package codes.laivy.proxy.http.core.headers;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.regex.Pattern;

public final class HeaderKey {

    // Static initializers

    public static final @NotNull Pattern NAME_FORMAT_REGEX = Pattern.compile("^[A-Za-z][A-Za-z0-9-]*$");

    public static @NotNull HeaderKey create(@NotNull String name) {
        try {
            @NotNull Field field = HeaderKey.class.getDeclaredField(name);
            return (HeaderKey) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException ignore) {
        }

        return create(name, null);
    }
    public static @NotNull HeaderKey create(@NotNull String name, @Nullable Pattern pattern) {
        return new HeaderKey(name, pattern);
    }

    // Provided

    public static @NotNull HeaderKey ACCEPT = HeaderKey.create("Accept");
    public static @NotNull HeaderKey ACCEPT_CH = HeaderKey.create("Accept-CH");
    @Deprecated
    public static @NotNull HeaderKey ACCEPT_CH_LIFETIME = HeaderKey.create("Accept-CH-Lifetime", Pattern.compile("^\\d+$"));
    public static @NotNull HeaderKey ACCEPT_CHARSET = HeaderKey.create("Accept-Charset");
    public static @NotNull HeaderKey ACCEPT_ENCODING = HeaderKey.create("Accept-Encoding");
    public static @NotNull HeaderKey ACCEPT_LANGUAGE = HeaderKey.create("Accept-Language");
    public static @NotNull HeaderKey ACCEPT_PATCH = HeaderKey.create("Accept-Patch");

    /**
     * @see <a href="https://regexr.com/7sft5">RegExr Tests</a>
     * @apiNote Last change: 23/02/2024 | 16:36 (GMT-3)
     */
    public static @NotNull HeaderKey ACCEPT_POST = HeaderKey.create("Accept-Post", Pattern.compile("^(?i)([a-zA-Z0-9+-.*]+/[a-zA-Z0-9+-.*]+(, *)?)+$"));
    public static @NotNull HeaderKey ACCEPT_RANGES = HeaderKey.create("Accept-Ranges");
    public static @NotNull HeaderKey ACCEPT_CONTROL_ALLOW_CREDENTIALS = HeaderKey.create("Access-Control-Allow-Credentials");
    public static @NotNull HeaderKey ACCEPT_CONTROL_ALLOW_HEADERS = HeaderKey.create("Access-Control-Allow-Headers");
    public static @NotNull HeaderKey ACCEPT_CONTROL_ALLOW_METHODS = HeaderKey.create("Access-Control-Allow-Methods");
    public static @NotNull HeaderKey ACCEPT_CONTROL_ALLOW_ORIGIN = HeaderKey.create("Access-Control-Allow-Origin");
    public static @NotNull HeaderKey ACCEPT_CONTROL_EXPOSE_HEADERS = HeaderKey.create("Access-Control-Expose-Headers");
    public static @NotNull HeaderKey ACCEPT_CONTROL_MAX_AGE = HeaderKey.create("Access-Control-Max-Age");
    public static @NotNull HeaderKey ACCEPT_CONTROL_REQUEST_HEADERS = HeaderKey.create("Access-Control-Request-Headers");
    public static @NotNull HeaderKey ACCEPT_CONTROL_REQUEST_METHOD = HeaderKey.create("Access-Control-Request-Method");
    public static @NotNull HeaderKey AGE = HeaderKey.create("Age", Pattern.compile("^\\d+$"));
    /**
     * @see <a href="https://regexr.com/7sftn">RegExr Tests</a>
     * @apiNote Last change: 23/02/2024 | 19:38 (GMT-3)
     */
    public static @NotNull HeaderKey ALLOW = HeaderKey.create("Allow", Pattern.compile("^(?i)(GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS|TRACE|CONNECT)(,[ ]?(GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS|TRACE|CONNECT))*?$"));
    public static @NotNull HeaderKey ALT_SVC = HeaderKey.create("Alt-Svc");
    public static @NotNull HeaderKey ALT_USED = HeaderKey.create("Alt-Used");
    public static @NotNull HeaderKey AUTHORIZATION = HeaderKey.create("Authorization");
    public static @NotNull HeaderKey CACHE_CONTROL = HeaderKey.create("Cache-Control");
    public static @NotNull HeaderKey CLEAR_SITE_DATA = HeaderKey.create("Clear-Site-Data");
    public static @NotNull HeaderKey CONNECTION = HeaderKey.create("Connection", Pattern.compile("^(?i)(keep-alive|close)(,\\s?[a-zA-Z0-9!#$%&'*+.^_`|~-]+)*$"));
    public static @NotNull HeaderKey CONTENT_DISPOSITION = HeaderKey.create("Content-Disposition");
    public static @NotNull HeaderKey CONTENT_DISPOSITION = HeaderKey.create("Content-DPR");
    public static @NotNull HeaderKey CONTENT_ENCODING = HeaderKey.create("Content-Encoding");
    public static @NotNull HeaderKey CONTENT_LANGUAGE = HeaderKey.create("Content-Language");
    public static @NotNull HeaderKey CONTENT_LENGTH = HeaderKey.create("Content-Length");
    public static @NotNull HeaderKey CONTENT_LOCATION = HeaderKey.create("Content-Location");
    public static @NotNull HeaderKey CONTENT_RANGE = HeaderKey.create("Content-Range");
    public static @NotNull HeaderKey CONTENT_SECURITY_POLICY = HeaderKey.create("Content-Security-Policy");
    public static @NotNull HeaderKey CONTENT_SECURITY_POLICY_REPORT_ONLY = HeaderKey.create("Content-Security-Policy-Report-Only");
    /**
     * @see <a href="https://regexr.com/7sfu0">RegExr Tests</a>
     * @apiNote Last change: 23/02/2024 | 19:06 (GMT-3)
     */
    public static @NotNull HeaderKey CONTENT_TYPE = HeaderKey.create("Content-Type", Pattern.compile("^[a-zA-Z0-9+-.*]+/[a-zA-Z0-9+-.*]+(?:; ?(boundary=[a-zA-Z0-9-]+|charset=[a-zA-Z0-9-]+))?(?:; ?(boundary=[a-zA-Z0-9-]+|charset=[a-zA-Z0-9-]+))?$"));
    public static @NotNull HeaderKey PROXY_CONNECTION = HeaderKey.create("Proxy-Connection", Pattern.compile("^(?i)(keep-alive|close)(,\\s?[a-zA-Z0-9!#$%&'*+.^_`|~-]+)*$"));
    public static @NotNull HeaderKey HOST = HeaderKey.create("Host", Pattern.compile("^([a-zA-Z0-9.-]+)(:([0-9]+))?$"));
    public static @NotNull HeaderKey WWW_AUTHENTICATE = HeaderKey.create("WWW-Authenticate");
    public static @NotNull HeaderKey PROXY_AUTHENTICATE = HeaderKey.create("Proxy-Authenticate");
    public static @NotNull HeaderKey PROXY_AUTHORIZATION = HeaderKey.create("Proxy-Authorization");

    /**
     * @see <a href="https://regexr.com/7sgub">RegExr Tests</a>
     * @apiNote Last change: 25/02/2024 | 01:43 (GMT-3)
     */
    public static @NotNull HeaderKey DATE = HeaderKey.create("Date", Pattern.compile("(Mon|Tue|Wed|Thu|Fri|Sat|Sun), ([0-2][0-9]|3[0-1]) (Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) (19[0-9]{2}|20[0-9]{2}) ([0-1][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9] GMT"));

    /**
     * @see <a href="https://regexr.com/7sg4c">RegExr Tests</a>
     * @apiNote Last change: 23/02/2024 | 19:23 (GMT-3)
     */
    public static @NotNull HeaderKey UPGRADE = HeaderKey.create("Upgrade", Pattern.compile("^[a-zA-Z-_]+(?:/[a-zA-Z0-9-_.@]+)?(?:,\\s?[a-zA-Z-_]+(?:/[a-zA-Z0-9-_.@]+)?)*$"));

    // todo: add more provided headers

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
