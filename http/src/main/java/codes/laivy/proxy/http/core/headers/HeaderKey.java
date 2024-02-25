package codes.laivy.proxy.http.core.headers;

import org.jetbrains.annotations.ApiStatus;
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
        return create(name, null);
    }
    public static @NotNull HeaderKey create(@NotNull String name, @Nullable Pattern pattern) {
        try {
            @NotNull Field field = HeaderKey.class.getDeclaredField(name.replace("-", "_").toLowerCase());
            return (HeaderKey) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException ignore) {
        } catch (Throwable throwable) {
            throw new IllegalStateException("Cannot create header key '" + name + "'");
        }

        return new HeaderKey(name, pattern);
    }

    // Provided

    public static @NotNull HeaderKey ACCEPT = new HeaderKey("Accept");
    public static @NotNull HeaderKey ACCEPT_CH = new HeaderKey("Accept-CH");
    @Deprecated
    public static @NotNull HeaderKey ACCEPT_CH_LIFETIME = new HeaderKey("Accept-CH-Lifetime", Pattern.compile("^\\d+$"));
    public static @NotNull HeaderKey ACCEPT_CHARSET = new HeaderKey("Accept-Charset");
    public static @NotNull HeaderKey ACCEPT_ENCODING = new HeaderKey("Accept-Encoding");
    public static @NotNull HeaderKey ACCEPT_LANGUAGE = new HeaderKey("Accept-Language");
    public static @NotNull HeaderKey ACCEPT_PATCH = new HeaderKey("Accept-Patch");

    /**
     * @see <a href="https://regexr.com/7sft5">RegExr Tests</a>
     * @apiNote Last change: 23/02/2024 | 16:36 (GMT-3)
     */
    public static @NotNull HeaderKey ACCEPT_POST = new HeaderKey("Accept-Post", Pattern.compile("^(?i)([a-zA-Z0-9+-.*]+/[a-zA-Z0-9+-.*]+(, *)?)+$"));
    public static @NotNull HeaderKey ACCEPT_RANGES = new HeaderKey("Accept-Ranges");
    public static @NotNull HeaderKey ACCEPT_CONTROL_ALLOW_CREDENTIALS = new HeaderKey("Access-Control-Allow-Credentials");
    public static @NotNull HeaderKey ACCEPT_CONTROL_ALLOW_HEADERS = new HeaderKey("Access-Control-Allow-Headers");
    public static @NotNull HeaderKey ACCEPT_CONTROL_ALLOW_METHODS = new HeaderKey("Access-Control-Allow-Methods");
    public static @NotNull HeaderKey ACCEPT_CONTROL_ALLOW_ORIGIN = new HeaderKey("Access-Control-Allow-Origin");
    public static @NotNull HeaderKey ACCEPT_CONTROL_EXPOSE_HEADERS = new HeaderKey("Access-Control-Expose-Headers");
    public static @NotNull HeaderKey ACCEPT_CONTROL_MAX_AGE = new HeaderKey("Access-Control-Max-Age");
    public static @NotNull HeaderKey ACCEPT_CONTROL_REQUEST_HEADERS = new HeaderKey("Access-Control-Request-Headers");
    public static @NotNull HeaderKey ACCEPT_CONTROL_REQUEST_METHOD = new HeaderKey("Access-Control-Request-Method");
    public static @NotNull HeaderKey AGE = new HeaderKey("Age", Pattern.compile("^\\d+$"));
    /**
     * @see <a href="https://regexr.com/7sftn">RegExr Tests</a>
     * @apiNote Last change: 23/02/2024 | 19:38 (GMT-3)
     */
    public static @NotNull HeaderKey ALLOW = new HeaderKey("Allow", Pattern.compile("^(?i)(GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS|TRACE|CONNECT)(,[ ]?(GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS|TRACE|CONNECT))*?$"));
    public static @NotNull HeaderKey ALT_SVC = new HeaderKey("Alt-Svc");
    public static @NotNull HeaderKey ALT_USED = new HeaderKey("Alt-Used");
    public static @NotNull HeaderKey AUTHORIZATION = new HeaderKey("Authorization");
    public static @NotNull HeaderKey CACHE_CONTROL = new HeaderKey("Cache-Control");
    public static @NotNull HeaderKey CLEAR_SITE_DATA = new HeaderKey("Clear-Site-Data");
    public static @NotNull HeaderKey CONNECTION = new HeaderKey("Connection", Pattern.compile("^(?i)(keep-alive|close)(,\\s?[a-zA-Z0-9!#$%&'*+.^_`|~-]+)*$"));
    public static @NotNull HeaderKey CONTENT_DISPOSITION = new HeaderKey("Content-Disposition");
    @Deprecated
    public static @NotNull HeaderKey CONTENT_DPR = new HeaderKey("Content-DPR");
    public static @NotNull HeaderKey CONTENT_ENCODING = new HeaderKey("Content-Encoding");
    public static @NotNull HeaderKey CONTENT_LANGUAGE = new HeaderKey("Content-Language");
    public static @NotNull HeaderKey CONTENT_LENGTH = new HeaderKey("Content-Length");
    public static @NotNull HeaderKey CONTENT_LOCATION = new HeaderKey("Content-Location");
    public static @NotNull HeaderKey CONTENT_RANGE = new HeaderKey("Content-Range");
    public static @NotNull HeaderKey CONTENT_SECURITY_POLICY = new HeaderKey("Content-Security-Policy");
    public static @NotNull HeaderKey CONTENT_SECURITY_POLICY_REPORT_ONLY = new HeaderKey("Content-Security-Policy-Report-Only");
    /**
     * @see <a href="https://regexr.com/7sfu0">RegExr Tests</a>
     * @apiNote Last change: 23/02/2024 | 19:06 (GMT-3)
     */
    public static @NotNull HeaderKey CONTENT_TYPE = new HeaderKey("Content-Type", Pattern.compile("^[a-zA-Z0-9+-.*]+/[a-zA-Z0-9+-.*]+(?:; ?(boundary=[a-zA-Z0-9-]+|charset=[a-zA-Z0-9-]+))?(?:; ?(boundary=[a-zA-Z0-9-]+|charset=[a-zA-Z0-9-]+))?$"));
    public static @NotNull HeaderKey COOKIE = new HeaderKey("Cookie");
    public static @NotNull HeaderKey CRITICAL_CH = new HeaderKey("Critical-CH");
    public static @NotNull HeaderKey CROSS_ORIGIN_EMBEDDER_POLICY = new HeaderKey("Cross-Origin-Embedder-Policy");
    public static @NotNull HeaderKey CROSS_ORIGIN_OPENER_POLICY = new HeaderKey("Cross-Origin-Opener-Policy");
    public static @NotNull HeaderKey CROSS_ORIGIN_RESOURCE_POLICY = new HeaderKey("Cross-Origin-Resource-Policy");
    /**
     * @see <a href="https://regexr.com/7sgub">RegExr Tests</a>
     * @apiNote Last change: 25/02/2024 | 01:43 (GMT-3)
     */
    public static @NotNull HeaderKey DATE = new HeaderKey("Date", Pattern.compile("(Mon|Tue|Wed|Thu|Fri|Sat|Sun), ([0-2][0-9]|3[0-1]) (Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) (19[0-9]{2}|20[0-9]{2}) ([0-1][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9] GMT"));
    public static @NotNull HeaderKey DEVICE_MEMORY = new HeaderKey("Device-Memory");
    @Deprecated
    public static @NotNull HeaderKey DIGEST = new HeaderKey("Digest");
    @Deprecated
    public static @NotNull HeaderKey DNT = new HeaderKey("DNT");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey DOWNLINK = new HeaderKey("Downlink");
    @Deprecated
    public static @NotNull HeaderKey DPR = new HeaderKey("DPR");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey EARLY_DATA = new HeaderKey("Early-Data");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey ECT = new HeaderKey("ECT");
    public static @NotNull HeaderKey ETAG = new HeaderKey("ETag");
    public static @NotNull HeaderKey EXPECT = new HeaderKey("Expect");
    public static @NotNull HeaderKey EXPECT_CT = new HeaderKey("Expect-CT");
    public static @NotNull HeaderKey EXPIRES = new HeaderKey("Expires");
    public static @NotNull HeaderKey FORWARDED = new HeaderKey("Forwarded");
    public static @NotNull HeaderKey FROM = new HeaderKey("From");
    public static @NotNull HeaderKey HOST = new HeaderKey("Host");
    public static @NotNull HeaderKey IF_MATCH = new HeaderKey("If-Match");
    public static @NotNull HeaderKey IF_MODIFIED_SINCE = new HeaderKey("If-Modified-Since");
    public static @NotNull HeaderKey IF_NONE_MATCH = new HeaderKey("If-None-Match");
    public static @NotNull HeaderKey IF_RANGE = new HeaderKey("If-Range");
    public static @NotNull HeaderKey IF_UNMODIFIED_SINCE = new HeaderKey("If-Unmodified-Since");
    public static @NotNull HeaderKey KEEP_ALIVE = new HeaderKey("Keep-Alive");
    @Deprecated
    public static @NotNull HeaderKey LARGE_ALLOCATION = new HeaderKey("Large-Allocation");
    public static @NotNull HeaderKey LAST_MODIFIED = new HeaderKey("Last-Modified");
    public static @NotNull HeaderKey LINK = new HeaderKey("Link");
    public static @NotNull HeaderKey LOCATION = new HeaderKey("Location");
    public static @NotNull HeaderKey MAX_FORWARDS = new HeaderKey("Max-Forwards");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey NEL = new HeaderKey("NEL");
    public static @NotNull HeaderKey OBSERVE_BROWSING_TOPICS = new HeaderKey("Observe-Browsing-Topics");
    public static @NotNull HeaderKey ORIGIN = new HeaderKey("Origin");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey ORIGIN_AGENT_CLUSTER = new HeaderKey("Origin-Agent-Cluster");
    public static @NotNull HeaderKey PERMISSIONS_POLICY = new HeaderKey("Permissions-Policy");
    @Deprecated
    public static @NotNull HeaderKey PRAGMA = new HeaderKey("Pragma");
    public static @NotNull HeaderKey PROXY_AUTHENTICATE = new HeaderKey("Proxy-Authenticate");
    public static @NotNull HeaderKey PROXY_AUTHORIZATION = new HeaderKey("Proxy-Authorization");
    public static @NotNull HeaderKey RANGE = new HeaderKey("Range");
    public static @NotNull HeaderKey REFERER = new HeaderKey("Referer");
    public static @NotNull HeaderKey REFERRER_POLICY = new HeaderKey("Referrer-Policy");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey RTT = new HeaderKey("RTT");
    public static @NotNull HeaderKey SAVE_DATA = new HeaderKey("Save-Data");
    public static @NotNull HeaderKey SEC_BROWSING_TOPICS = new HeaderKey("Sec-Browsing-Topics");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey SEC_CH_PREFERS_COLOR_SCHEME = new HeaderKey("Sec-CH-Prefers-Color-Scheme");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey SEC_CH_PREFERS_REDUCED_MOTION = new HeaderKey("Sec-CH-Prefers-Reduced-Motion");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey SEC_CH_PREFERS_REDUCED_TRANSPARENCY = new HeaderKey("Sec-CH-Prefers-Reduced-Transparency");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey SEC_CH_UA = new HeaderKey("Sec-CH-UA");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey SEC_CH_UA_ARCH = new HeaderKey("Sec-CH-UA-Arch");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey SEC_CH_UA_BITNESS = new HeaderKey("Sec-CH-UA-Bitness");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey SEC_CH_UA_FULL_VERSION = new HeaderKey("Sec-CH-UA-Full-Version");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey SEC_CH_UA_FULL_VERSION_LIST = new HeaderKey("Sec-CH-UA-Full-Version-List");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey SEC_CH_UA_MOBILE = new HeaderKey("Sec-CH-UA-Mobile");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey SEC_CH_UA_MODEL = new HeaderKey("Sec-CH-UA-Model");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey SEC_CH_UA_PLATFORM = new HeaderKey("Sec-CH-UA-Platform");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey SEC_CH_UA_PLATFORM_VERSION = new HeaderKey("Sec-CH-UA-Platform-Version");
    public static @NotNull HeaderKey SEC_FETCH_DEST = new HeaderKey("Sec-Fetch-Dest");
    public static @NotNull HeaderKey SEC_FETCH_MODE = new HeaderKey("Sec-Fetch-Mode");
    public static @NotNull HeaderKey SEC_FETCH_SITE = new HeaderKey("Sec-Fetch-Site");
    public static @NotNull HeaderKey SEC_FETCH_USER = new HeaderKey("Sec-Fetch-User");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey SEC_GPC = new HeaderKey("Sec-GPC");
    public static @NotNull HeaderKey SEC_PURPOSE = new HeaderKey("Sec-Purpose");
    public static @NotNull HeaderKey SEC_WEBSOCKET_ACCEPT = new HeaderKey("Sec-WebSocket-Accept");
    public static @NotNull HeaderKey SERVER = new HeaderKey("Server");
    public static @NotNull HeaderKey SERVER_TIMING = new HeaderKey("Server-Timing");
    public static @NotNull HeaderKey SERVICE_WORKER_NAVIGATION_PRELOAD = new HeaderKey("Service-Worker-Navigation-Preload");
    public static @NotNull HeaderKey SET_COOKIE = new HeaderKey("Set-Cookie");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey SET_LOGIN = new HeaderKey("Set-Login");
    public static @NotNull HeaderKey SOURCEMAP = new HeaderKey("SourceMap");
    public static @NotNull HeaderKey STRICT_TRANSPORT_SECURITY = new HeaderKey("Strict-Transport-Security");
    public static @NotNull HeaderKey SUPPORTS_LOADING_MODE = new HeaderKey("Supports-Loading-Mode");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey TE = new HeaderKey("TE");
    public static @NotNull HeaderKey TIMING_ALLOW_ORIGIN = new HeaderKey("Timing-Allow-Origin");
    @Deprecated
    public static @NotNull HeaderKey TK = new HeaderKey("Tk");
    public static @NotNull HeaderKey TRAILER = new HeaderKey("Trailer");
    public static @NotNull HeaderKey TRANSFER_ENCODING = new HeaderKey("Transfer-Encoding");
    public static @NotNull HeaderKey ANONYMOUS_HEADER = new HeaderKey("X-Anonymous", Pattern.compile("^(?i)(true|false)$"));

    /**
     * @see <a href="https://regexr.com/7sg4c">RegExr Tests</a>
     * @apiNote Last change: 23/02/2024 | 19:23 (GMT-3)
     */
    public static @NotNull HeaderKey UPGRADE = new HeaderKey("Upgrade", Pattern.compile("^[a-zA-Z-_]+(?:/[a-zA-Z0-9-_.@]+)?(?:,\\s?[a-zA-Z-_]+(?:/[a-zA-Z0-9-_.@]+)?)*$"));
    public static @NotNull HeaderKey UPGRADE_INSECURE_REQUESTS = new HeaderKey("Upgrade-Insecure-Requests");
    public static @NotNull HeaderKey USER_AGENT = new HeaderKey("User-Agent");
    public static @NotNull HeaderKey VARY = new HeaderKey("Vary");
    public static @NotNull HeaderKey VIA = new HeaderKey("Via");
    @Deprecated
    public static @NotNull HeaderKey VIEWPORT_WIDTH = new HeaderKey("Viewport-Width");
    @Deprecated
    public static @NotNull HeaderKey WANT_DIGEST = new HeaderKey("Want-Digest");
    @Deprecated
    public static @NotNull HeaderKey WARNING = new HeaderKey("Warning");
    @Deprecated
    public static @NotNull HeaderKey WIDTH = new HeaderKey("Width");
    public static @NotNull HeaderKey WWW_AUTHENTICATE = new HeaderKey("WWW-Authenticate");
    public static @NotNull HeaderKey PROXY_CONNECTION = new HeaderKey("Proxy-Connection", Pattern.compile("^(?i)(keep-alive|close)(,\\s?[a-zA-Z0-9!#$%&'*+.^_`|~-]+)*$"));

    // todo: add more provided headers

    // Object

    private final @NotNull String name;
    private final @Nullable Pattern pattern;

    private HeaderKey(@NotNull String name) {
        this(name, null);
    }
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
