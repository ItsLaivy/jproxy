package codes.laivy.proxy.http.core;

import codes.laivy.proxy.http.connection.HttpProxyClient;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Base64;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The authorization class is used to allow only users who provide some degree of authentication to use the proxy
 *
 * @since 1.0-SNAPSHOT
 */
public interface HttpAuthorization {

    /**
     * Creates an authorization object that uses the bearer token scheme
     * <p style="color:red">Note: After the first successfully validation, it removes the authorization header. It means if you try to validate again, it will return false.</p>
     *
     * @param predicate a function that checks if the token is valid
     * @return an authentication object that implements the bearer token logic
     * @author Daniel Richard (Laivy)
     * @see <a href="https://apidog.com/articles/what-is-bearer-token/">Bearer Authorization</a>
     * @since 1.0-SNAPSHOT
     */
    static @NotNull HttpAuthorization bearer(final @NotNull String headerName, @NotNull Predicate<String> predicate) {
        // Bad Request (400)
        @NotNull HttpResponse bad = new BasicHttpResponse(400, "bad request");
        bad.setVersion(HttpVersion.HTTP_1_1);
        // Unauthorized (401)
        @NotNull HttpResponse unauthorized = new BasicHttpResponse(401, "unauthorized");
        unauthorized.addHeader(new BasicHeader("WWW-Authenticate", "Bearer"));
        unauthorized.setVersion(HttpVersion.HTTP_1_1);
        // Missing authentication (407)
        @NotNull HttpResponse missing = new BasicHttpResponse(407, "missing proxy authentication");
        missing.addHeader(new BasicHeader("Proxy-Authenticate", "Bearer"));
        missing.setVersion(HttpVersion.HTTP_1_1);

        // Authorization
        return (socket, request) -> {
            try {
                if (!request.containsHeader(headerName)) {
                    return missing;
                }

                @NotNull String[] auth = request.getLastHeader(headerName).getValue().split(" ");

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
                request.removeHeaders(headerName);
            }
        };
    }

    /**
     * Creates an authorization object that uses the basic token scheme
     * <p style="color:red">Note: After the first successfully validation, it removes the authorization header. It means if you try to validate again, it will return false.</p>
     *
     * @param predicate a function that checks if the token is valid
     * @return an authentication object that implements the bearer token logic
     * @author Daniel Richard (Laivy)
     * @see <a href="https://en.wikipedia.org/wiki/Basic_access_authentication">Basic Authorization</a>
     * @since 1.0-SNAPSHOT
     */
    static @NotNull HttpAuthorization basic(final @NotNull String headerName, @NotNull Predicate<UsernamePasswordCredentials> predicate) {
        // Bad Request (400)
        @NotNull HttpResponse bad = new BasicHttpResponse(400, "bad request");
        bad.setVersion(HttpVersion.HTTP_1_1);
        // Unauthorized (401)
        @NotNull HttpResponse unauthorized = new BasicHttpResponse(401, "unauthorized");
        unauthorized.addHeader(new BasicHeader("WWW-Authenticate", "Bearer"));
        unauthorized.setVersion(HttpVersion.HTTP_1_1);
        // Missing authentication (407)
        @NotNull HttpResponse missing = new BasicHttpResponse(407, "missing proxy authentication");
        missing.addHeader(new BasicHeader("Proxy-Authenticate", "Bearer"));
        missing.setVersion(HttpVersion.HTTP_1_1);

        // Authorization

        return (socket, request) -> {
            try {
                if (!request.containsHeader(headerName)) {
                    return missing;
                }

                @NotNull String[] auth = request.getLastHeader(headerName).getValue().split(" ");

                if (auth.length < 2) {
                    return unauthorized;
                } else if (!auth[0].equalsIgnoreCase("Basic")) {
                    return unauthorized;
                }

                @NotNull String encoded = Arrays.stream(auth).skip(1).map(string -> string + " ").collect(Collectors.joining());
                @NotNull String[] decoded = new String(Base64.getDecoder().decode(encoded)).split(":");

                return predicate.test(new UsernamePasswordCredentials(decoded[0], decoded[1].toCharArray())) ? null : unauthorized;
            } catch (@NotNull Throwable ignore) {
                return bad;
            } finally {
                request.removeHeaders(headerName);
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
