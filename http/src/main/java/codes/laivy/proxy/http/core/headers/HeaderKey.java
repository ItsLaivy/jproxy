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
    @Deprecated
    public static @NotNull HeaderKey CONTENT_DPR = HeaderKey.create("Content-DPR");
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
    public static @NotNull HeaderKey COOKIE = HeaderKey.create("Cookie");
    public static @NotNull HeaderKey CRITICAL_CH = HeaderKey.create("Critical-CH");
    public static @NotNull HeaderKey CROSS_ORIGIN_EMBEDDER_POLICY = HeaderKey.create("Cross-Origin-Embedder-Policy");
    public static @NotNull HeaderKey CROSS_ORIGIN_OPENER_POLICY = HeaderKey.create("Cross-Origin-Opener-Policy");
    public static @NotNull HeaderKey CROSS_ORIGIN_RESOURCE_POLICY = HeaderKey.create("Cross-Origin-Resource-Policy");
    /**
     * @see <a href="https://regexr.com/7sgub">RegExr Tests</a>
     * @apiNote Last change: 25/02/2024 | 01:43 (GMT-3)
     */
    public static @NotNull HeaderKey DATE = HeaderKey.create("Date", Pattern.compile("(Mon|Tue|Wed|Thu|Fri|Sat|Sun), ([0-2][0-9]|3[0-1]) (Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) (19[0-9]{2}|20[0-9]{2}) ([0-1][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9] GMT"));
    public static @NotNull HeaderKey DEVICE_MEMORY = HeaderKey.create("Device-Memory");
    @Deprecated
    public static @NotNull HeaderKey DIGEST = HeaderKey.create("Digest");
    @Deprecated
    public static @NotNull HeaderKey DNT = HeaderKey.create("DNT");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey DOWNLINK = HeaderKey.create("Downlink");
    @Deprecated
    public static @NotNull HeaderKey DPR = HeaderKey.create("DPR");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey EARLY_DATA = HeaderKey.create("Early-Data");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey ECT = HeaderKey.create("ECT");
    public static @NotNull HeaderKey ETAG = HeaderKey.create("ETag");
    public static @NotNull HeaderKey EXPECT = HeaderKey.create("Expect");
    public static @NotNull HeaderKey EXPECT_CT = HeaderKey.create("Expect-CT");
    public static @NotNull HeaderKey EXPIRES = HeaderKey.create("Expires");
    public static @NotNull HeaderKey FORWARDED = HeaderKey.create("Forwarded");
    public static @NotNull HeaderKey FROM = HeaderKey.create("From");
    public static @NotNull HeaderKey HOST = HeaderKey.create("Host");
    public static @NotNull HeaderKey IF_MATCH = HeaderKey.create("If-Match");
    public static @NotNull HeaderKey IF_MODIFIED_SINCE = HeaderKey.create("If-Modified-Since");
    public static @NotNull HeaderKey IF_NONE_MATCH = HeaderKey.create("If-None-Match");
    public static @NotNull HeaderKey IF_RANGE = HeaderKey.create("If-Range");
    public static @NotNull HeaderKey IF_UNMODIFIED_SINCE = HeaderKey.create("If-Unmodified-Since");
    public static @NotNull HeaderKey KEEP_ALIVE = HeaderKey.create("Keep-Alive");
    @Deprecated
    public static @NotNull HeaderKey LARGE_ALLOCATION = HeaderKey.create("Large-Allocation");
    public static @NotNull HeaderKey LAST_MODIFIED = HeaderKey.create("Last-Modified");
    public static @NotNull HeaderKey LINK = HeaderKey.create("Link");
    public static @NotNull HeaderKey LOCATION = HeaderKey.create("Location");
    public static @NotNull HeaderKey MAX_FORWARDS = HeaderKey.create("Max-Forwards");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey NEL = HeaderKey.create("NEL");
    public static @NotNull HeaderKey OBSERVE_BROWSING_TOPICS = HeaderKey.create("Observe-Browsing-Topics");
    public static @NotNull HeaderKey ORIGIN = HeaderKey.create("Origin");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey ORIGIN_AGENT_CLUSTER = HeaderKey.create("Origin-Agent-Cluster");
    public static @NotNull HeaderKey PERMISSIONS_POLICY = HeaderKey.create("Permissions-Policy");
    @Deprecated
    public static @NotNull HeaderKey PRAGMA = HeaderKey.create("Pragma");
    public static @NotNull HeaderKey PROXY_AUTHENTICATE = HeaderKey.create("Proxy-Authenticate");
    public static @NotNull HeaderKey PROXY_AUTHORIZATION = HeaderKey.create("Proxy-Authorization");
    public static @NotNull HeaderKey RANGE = HeaderKey.create("Range");
    public static @NotNull HeaderKey REFERER = HeaderKey.create("Referer");
    public static @NotNull HeaderKey REFERRER_POLICY = HeaderKey.create("Referrer-Policy");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey RTT = HeaderKey.create("RTT");
    public static @NotNull HeaderKey SAVE_DATA = HeaderKey.create("Save-Data");
    public static @NotNull HeaderKey SEC_BROWSING_TOPICS = HeaderKey.create("Sec-Browsing-Topics");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey SEC_CH_PREFERS_COLOR_SCHEME = HeaderKey.create("Sec-CH-Prefers-Color-Scheme");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey SEC_CH_PREFERS_REDUCED_MOTION = HeaderKey.create("Sec-CH-Prefers-Reduced-Motion");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey SEC_CH_PREFERS_REDUCED_TRANSPARENCY = HeaderKey.create("Sec-CH-Prefers-Reduced-Transparency");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey SEC_CH_UA = HeaderKey.create("Sec-CH-UA");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey SEC_CH_UA_ARCH = HeaderKey.create("Sec-CH-UA-Arch");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey SEC_CH_UA_BITNESS = HeaderKey.create("Sec-CH-UA-Bitness");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey SEC_CH_UA_FULL_VERSION = HeaderKey.create("Sec-CH-UA-Full-Version");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey SEC_CH_UA_FULL_VERSION_LIST = HeaderKey.create("Sec-CH-UA-Full-Version-List");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey SEC_CH_UA_MOBILE = HeaderKey.create("Sec-CH-UA-Mobile");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey SEC_CH_UA_MODEL = HeaderKey.create("Sec-CH-UA-Model");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey SEC_CH_UA_PLATFORM = HeaderKey.create("Sec-CH-UA-Platform");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey SEC_CH_UA_PLATFORM_VERSION = HeaderKey.create("Sec-CH-UA-Platform-Version");
    public static @NotNull HeaderKey SEC_FETCH_DEST = HeaderKey.create("Sec-Fetch-Dest");
    public static @NotNull HeaderKey SEC_FETCH_MODE = HeaderKey.create("Sec-Fetch-Mode");
    public static @NotNull HeaderKey SEC_FETCH_SITE = HeaderKey.create("Sec-Fetch-Site");
    public static @NotNull HeaderKey SEC_FETCH_USER = HeaderKey.create("Sec-Fetch-User");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey SEC_GPC = HeaderKey.create("Sec-GPC");
    public static @NotNull HeaderKey SEC_PURPOSE = HeaderKey.create("Sec-Purpose");
    public static @NotNull HeaderKey SEC_WEBSOCKET_ACCEPT = HeaderKey.create("Sec-WebSocket-Accept");
    public static @NotNull HeaderKey SERVER = HeaderKey.create("Server");
    public static @NotNull HeaderKey SERVER_TIMING = HeaderKey.create("Server-Timing");
    public static @NotNull HeaderKey SERVICE_WORKER_NAVIGATION_PRELOAD = HeaderKey.create("Service-Worker-Navigation-Preload");
    public static @NotNull HeaderKey SET_COOKIE = HeaderKey.create("Set-Cookie");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey SET_LOGIN = HeaderKey.create("Set-Login");
    public static @NotNull HeaderKey SOURCEMAP = HeaderKey.create("SourceMap");
    public static @NotNull HeaderKey STRICT_TRANSPORT_SECURITY = HeaderKey.create("Strict-Transport-Security");
    public static @NotNull HeaderKey SUPPORTS_LOADING_MODE = HeaderKey.create("Supports-Loading-Mode");
    @ApiStatus.Experimental
    public static @NotNull HeaderKey TE = HeaderKey.create("TE");
    public static @NotNull HeaderKey TIMING_ALLOW_ORIGIN = HeaderKey.create("Timing-Allow-Origin");
    @Deprecated
    public static @NotNull HeaderKey TK = HeaderKey.create("Tk");
    public static @NotNull HeaderKey TRAILER = HeaderKey.create("Trailer");
    public static @NotNull HeaderKey TRANSFER_ENCODING = HeaderKey.create("Transfer-Encoding");

    /**
     * @see <a href="https://regexr.com/7sg4c">RegExr Tests</a>
     * @apiNote Last change: 23/02/2024 | 19:23 (GMT-3)
     */
    public static @NotNull HeaderKey UPGRADE = HeaderKey.create("Upgrade", Pattern.compile("^[a-zA-Z-_]+(?:/[a-zA-Z0-9-_.@]+)?(?:,\\s?[a-zA-Z-_]+(?:/[a-zA-Z0-9-_.@]+)?)*$"));
    public static @NotNull HeaderKey UPGRADE_INSECURE_REQUESTS = HeaderKey.create("Upgrade-Insecure-Requests");
    public static @NotNull HeaderKey USER_AGENT = HeaderKey.create("User-Agent");
    public static @NotNull HeaderKey VARY = HeaderKey.create("Vary");
    public static @NotNull HeaderKey VIA = HeaderKey.create("Via");
    @Deprecated
    public static @NotNull HeaderKey VIEWPORT_WIDTH = HeaderKey.create("Viewport-Width");
    @Deprecated
    public static @NotNull HeaderKey WANT_DIGEST = HeaderKey.create("Want-Digest");
    @Deprecated
    public static @NotNull HeaderKey WARNING = HeaderKey.create("Warning");
    @Deprecated
    public static @NotNull HeaderKey WIDTH = HeaderKey.create("Width");
    public static @NotNull HeaderKey WWW_AUTHENTICATE = HeaderKey.create("WWW-Authenticate");
    public static @NotNull HeaderKey PROXY_CONNECTION = HeaderKey.create("Proxy-Connection", Pattern.compile("^(?i)(keep-alive|close)(,\\s?[a-zA-Z0-9!#$%&'*+.^_`|~-]+)*$"));

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
