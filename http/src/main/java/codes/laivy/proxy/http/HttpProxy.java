package codes.laivy.proxy.http;

import codes.laivy.proxy.http.impl.HttpProxyImpl;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class HttpProxy extends Proxy {

    // Initializers

    public static void main(String[] args) {
    }

    public static @NotNull HttpProxy create(@NotNull InetSocketAddress address, @Nullable Authorization authorization) throws IOException {
        return new HttpProxyImpl(address, authorization);
    }

    // Object

    private final @NotNull InetSocketAddress address;
    private final @Nullable Authorization authorization;

    protected HttpProxy(@NotNull InetSocketAddress address, @Nullable Authorization authorization) {
        super(Type.HTTP, address);

        this.address = address;
        this.authorization = authorization;
    }

    // Getters

    public final @NotNull InetSocketAddress getAddress() {
        return address;
    }

    public abstract @Nullable ServerSocket getServer();

    /**
     * A proxy authentication can be used to allow only certain users. If the authentication is null, the user who makes a request using it will not need to provide the authentication details
     * @return the authentication object or null if none is required
     */
    public @Nullable Authorization getAuthentication() {
        return this.authorization;
    }

    /**
     * This method is responsible for making an HTTP request directly using the proxy, this method does not check
     * the authentication of {@link #getAuthentication()}.
     *
     * @since 1.0-SNAPSHOT
     * @author Daniel Richard (Laivy)
     *
     * @param request the HTTP request to be sent
     * @return the HTTP response received from the server
     * @throws IOException if there is an error in the socket connection or the input/output streams
     * @throws HttpException if there is an error in the request or response serialization or parsing
     */
    @Blocking
    public abstract @NotNull HttpResponse request(@NotNull Socket socket, @NotNull HttpRequest request) throws IOException, HttpException;

    // Loaders

    public abstract boolean start() throws Exception;

    public abstract boolean stop() throws Exception;

    // java.net.Proxy natives

    @Override
    public final SocketAddress address() {
        return getAddress();
    }
    @Override
    public final @NotNull Type type() {
        return Type.HTTP;
    }

    @Override
    public @NotNull String toString() {
        return "HttpProxy " + address;
    }

    // Classes

    /**
     * The authorization class is used to allow only users who provide some degree of authentication to use the proxy
     * @since 1.0-SNAPSHOT
     */
    public interface Authorization {

        /**
         * Creates an authorization object that uses the bearer token scheme
         * <p style="color:red">Note: After the first successfully validation, it removes the authorization header. It means if you try to validate again, it will return false.</p>
         *
         * @since 1.0-SNAPSHOT
         * @author Daniel Richard (Laivy)
         *
         * @see <a href="https://apidog.com/articles/what-is-bearer-token/">Bearer Authorization</a>
         * @param predicate a function that checks if the token is valid
         * @return an authentication object that implements the bearer token logic
         */
        static @NotNull Authorization bearer(@NotNull Predicate<String> predicate) {
            final @NotNull String headerName = "Proxy-Authorization";

            return (socket, request) -> {
                try {
                    if (!request.containsHeader(headerName)) {
                        return false;
                    }

                    @NotNull String[] auth = request.getLastHeader(headerName).getValue().split(" ");

                    if (auth.length < 2) {
                        return false;
                    } else if (!auth[0].equalsIgnoreCase("Bearer")) {
                        return false;
                    }

                    int row = 0;
                    @NotNull StringBuilder merged = new StringBuilder();
                    for (@NotNull String part : Arrays.stream(auth).skip(1).toArray(String[]::new)) {
                        if (row > 0) merged.append(" ");
                        merged.append(part);
                        row++;
                    }

                    return predicate.test(merged.toString());
                } catch (@NotNull Throwable ignore) {
                    return false;
                } finally {
                    request.removeHeaders(headerName);
                }
            };
        }

        /**
         * Creates an authorization object that uses the basic token scheme
         * <p style="color:red">Note: After the first successfully validation, it removes the authorization header. It means if you try to validate again, it will return false.</p>
         *
         * @since 1.0-SNAPSHOT
         * @author Daniel Richard (Laivy)
         *
         * @see <a href="https://en.wikipedia.org/wiki/Basic_access_authentication">Basic Authorization</a>
         * @param predicate a function that checks if the token is valid
         * @return an authentication object that implements the bearer token logic
         */
        static @NotNull Authorization basic(@NotNull Predicate<UsernamePasswordCredentials> predicate) {
            final @NotNull String headerName = "Proxy-Authorization";

            return (socket, request) -> {
                try {
                    if (!request.containsHeader(headerName)) {
                        return false;
                    }

                    @NotNull String[] auth = request.getLastHeader(headerName).getValue().split(" ");

                    if (auth.length < 2) {
                        return false;
                    } else if (!auth[0].equalsIgnoreCase("Basic")) {
                        return false;
                    }

                    @NotNull String encoded = Arrays.stream(auth).skip(1).map(string -> string + " ").collect(Collectors.joining());
                    @NotNull String[] decoded = new String(Base64.getDecoder().decode(encoded)).split(":");

                    return predicate.test(new UsernamePasswordCredentials(decoded[0], decoded[1].toCharArray()));
                } catch (@NotNull Throwable ignore) {
                    return false;
                } finally {
                    request.removeHeaders(headerName);
                }
            };
        }

        /**
         * Validates the authorization by analyzing the socket and the request, returns true if the authentication was approved and the user is able to use the proxy or false otherwise. This method is invoked whenever a new request is made
         *
         * @since 1.0-SNAPSHOT
         * @author Daniel Richard (Laivy)
         *
         * @param socket the socket that connects the client and the proxy
         * @param request the HTTP request sent by the client
         * @return true if the authentication is valid, false otherwise
         */
        boolean validate(@NotNull Socket socket, @NotNull HttpRequest request);

    }

}
