package codes.laivy.proxy.http.core;

import codes.laivy.proxy.http.core.Credentials.Basic;
import org.jetbrains.annotations.*;

import java.net.IDN;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class URIAuthority {

    // Static initializers

    @ApiStatus.Internal
    private static final @NotNull Pattern PARSE_PATTERN = Pattern.compile("^(https?://)?(([^:@]*:[^@]*)@)?([^:/]*)(:[0-9]+)?(/.*)?$");

    public static final int DEFAULT_HTTP_PORT = 80;
    public static final int DEFAULT_HTTPS_PORT = 443;

    public static @NotNull URIAuthority parse(@NotNull String uri) throws URISyntaxException, UnknownHostException {
        @Nullable Basic userInfo = null;
        @NotNull String hostName;
        int port;

        @NotNull Pattern pattern = PARSE_PATTERN;
        @NotNull Matcher matcher = pattern.matcher(uri);

        if (matcher.find()) {
            @NotNull String scheme = matcher.group(1);

            try {
                @Nullable String temp = matcher.group(3);
                if (temp != null) userInfo = Basic.parse(temp);
            } catch (@NotNull ParseException throwable) {
                throw new URISyntaxException(uri, throwable.getMessage(), matcher.start(3));
            }

            hostName = IDN.toASCII(matcher.group(4));

            if (matcher.group(5) != null) {
                port = Integer.parseInt(matcher.group(5).substring(1));

                if (port < 0 || port > 65535) {
                    throw new URISyntaxException(uri, "port out of range '" + port + "'", matcher.start(5) + 1);
                }
            } else {
                if ("https://".equals(scheme)) {
                    port = DEFAULT_HTTPS_PORT;
                } else {
                    port = DEFAULT_HTTP_PORT;
                }
            }
        } else {
            throw new URISyntaxException(uri, "cannot parse into a valid uri authority", matcher.start());
        }

        return new URIAuthority(userInfo, InetSocketAddress.createUnresolved(hostName, port));
    }

    public static @NotNull URIAuthority create(@NotNull Basic userInfo, @NotNull InetSocketAddress address) {
        return new URIAuthority(userInfo, address);
    }
    public static @NotNull URIAuthority create(@NotNull String address, @Range(from = 0, to = 65535) int port) {
        return new URIAuthority(null, InetSocketAddress.createUnresolved(address, port));
    }

    // Object

    private final @Nullable Basic userInfo;
    private final @NotNull InetSocketAddress address;

    private URIAuthority(@Nullable Basic userInfo, @NotNull InetSocketAddress address) {
        this.userInfo = userInfo;
        this.address = address;
    }

    // Getters

    public @Nullable Basic getUserInfo() {
        return userInfo;
    }

    @Contract(pure = true)
    public @NotNull InetSocketAddress getAddress() {
        return address;
    }

    @Contract(pure = true)
    public @NotNull String getHostName() {
        return getAddress().getHostName();
    }
    @Contract(pure = true)
    @Range(from = 0, to = 65535)
    public int getPort() {
        return getAddress().getPort();
    }

    // Implementations

    @Override
    public @NotNull String toString() {
        return super.toString();
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (this == object) return true;
        if (!(object instanceof URIAuthority)) return false;
        URIAuthority authority = (URIAuthority) object;
        return Objects.equals(getUserInfo(), authority.getUserInfo()) && Objects.equals(getAddress(), authority.getAddress());
    }
    @Override
    public int hashCode() {
        return Objects.hash(getUserInfo(), getAddress());
    }

}
