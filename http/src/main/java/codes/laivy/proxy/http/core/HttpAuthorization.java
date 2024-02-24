package codes.laivy.proxy.http.core;

import codes.laivy.proxy.http.connection.HttpProxyClient;
import codes.laivy.proxy.http.core.Credentials.Basic;
import codes.laivy.proxy.http.core.headers.Header;
import codes.laivy.proxy.http.core.headers.HeaderKey;
import codes.laivy.proxy.http.core.protocol.HttpVersion;
import codes.laivy.proxy.http.core.request.HttpRequest;
import codes.laivy.proxy.http.core.response.HttpResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Base64;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The authorization class is used to allow only users who provide some degree of authentication to use the proxy
 *
 * @author Daniel Richard (Laivy)
 * @since 1.0-SNAPSHOT
 */
public interface HttpAuthorization {

    /**
     * Creates an authorization object that uses the bearer token scheme
     * <p style="color:red">Note: After the first successfully validation, it removes the authorization header. It means if you try to validate again, it will return false.</p>
     *
     * @param predicate a function that checks if the token is valid
     * @return an authentication object that implements the bearer token logic
     * @see <a href="https://apidog.com/articles/what-is-bearer-token/">Bearer Authorization</a>
     *
     * @author Daniel Richard (Laivy)
     * @since 1.0-SNAPSHOT
     */
    static @NotNull HttpAuthorization bearer(final @NotNull HeaderKey key, @NotNull Predicate<String> predicate) {
        // Bad Request (400)
        @NotNull HttpResponse bad = HttpStatus.BAD_REQUEST.createResponse(HttpVersion.HTTP1_1());
        // Unauthorized (401)
        @NotNull HttpResponse unauthorized = HttpStatus.UNAUTHORIZED.createResponse(HttpVersion.HTTP1_1());
        unauthorized.getHeaders().add(Header.create(HeaderKey.WWW_AUTHENTICATE, "Bearer"));
        // Missing authentication (407)
        @NotNull HttpResponse missing = HttpStatus.PROXY_AUTHENTICATION_REQUIRED.createResponse(HttpVersion.HTTP1_1());
        missing.getHeaders().add(Header.create(HeaderKey.PROXY_AUTHENTICATE, "Bearer"));
        // Authorization
        return (socket, request) -> {
            try {
                @Nullable Header header = request.getHeaders().first(key).orElse(null);
                if (header == null) return missing;

                @NotNull String[] auth = header.getValue().split(" ");

                if (auth.length < 2) {
                    return unauthorized;
                } else if (!auth[0].equalsIgnoreCase("Bearer")) {
                    return unauthorized;
                }

                int row = 0;
                @NotNull StringBuilder merged = new StringBuilder();
                for (@NotNull String part : Arrays.stream(auth).skip(1).toArray(String[]::new)) {
                    if (row > 0) merged.append(" ");
                    merged.append(part);
                    row++;
                }

                return predicate.test(merged.toString()) ? null : unauthorized;
            } catch (@NotNull Throwable ignore) {
                return bad;
            } finally {
                request.getHeaders().remove(key);
            }
        };
    }

    /**
     * Creates an authorization object that uses the basic token scheme
     * <p style="color:red">Note: After the first successfully validation, it removes the authorization header. It means if you try to validate again, it will return false.</p>
     *
     * @param predicate a function that checks if the token is valid
     * @return an authentication object that implements the bearer token logic
     * @see <a href="https://en.wikipedia.org/wiki/Basic_access_authentication">Basic Authorization</a>
     *
     * @author Daniel Richard (Laivy)\
     * @since 1.0-SNAPSHOT
     */
    static @NotNull HttpAuthorization basic(final @NotNull HeaderKey key, @NotNull Predicate<Basic> predicate) {
        // Bad Request (400)
        @NotNull HttpResponse bad = HttpStatus.BAD_REQUEST.createResponse(HttpVersion.HTTP1_1());
        // Unauthorized (401)
        @NotNull HttpResponse unauthorized = HttpStatus.UNAUTHORIZED.createResponse(HttpVersion.HTTP1_1());
        unauthorized.getHeaders().add(Header.create(HeaderKey.WWW_AUTHENTICATE, "Basic"));
        // Missing authentication (407)
        @NotNull HttpResponse missing = HttpStatus.PROXY_AUTHENTICATION_REQUIRED.createResponse(HttpVersion.HTTP1_1());
        missing.getHeaders().add(Header.create(HeaderKey.PROXY_AUTHENTICATE, "Basic"));

        // Authorization
        return (socket, request) -> {
            try {
                @Nullable Header header = request.getHeaders().first(key).orElse(null);
                if (header == null) return missing;

                @NotNull String[] auth = header.getValue().split(" ");

                if (auth.length < 2) {
                    return unauthorized;
                } else if (!auth[0].equalsIgnoreCase("Basic")) {
                    return unauthorized;
                }

                @NotNull String encoded = Arrays.stream(auth).skip(1).map(string -> string + " ").collect(Collectors.joining());
                @NotNull String[] decoded = new String(Base64.getDecoder().decode(encoded)).split(":");

                return predicate.test(new Basic(decoded[0], decoded[1].toCharArray())) ? null : unauthorized;
            } catch (@NotNull Throwable ignore) {
                return bad;
            } finally {
                request.getHeaders().remove(key);
            }
        };
    }

    /**
     * Validates the authorization by analyzing the client and the request, returns null if the authentication was approved and the user is able to use the proxy or an error http response otherwise. This method normally is invoked whenever a new request is made
     *
     * @param client  the client that connects the proxy
     * @param request the HTTP request sent by the client
     * @return an http response if the authentication fails, null otherwise.
     * @author Daniel Richard (Laivy)
     * @since 1.0-SNAPSHOT
     */
    @Nullable HttpResponse validate(@NotNull HttpProxyClient client, @NotNull HttpRequest request);

}
