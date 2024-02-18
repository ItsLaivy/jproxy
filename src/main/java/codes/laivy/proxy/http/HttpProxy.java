package codes.laivy.proxy.http;

import codes.laivy.proxy.Proxy;
import codes.laivy.proxy.http.impl.HttpProxyImpl;
import codes.laivy.proxy.http.utils.HttpUtils;
import org.apache.http.*;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Base64;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface HttpProxy extends Proxy {

    // Initializers

    static @NotNull HttpProxy create(@NotNull Authentication authentication, @NotNull InetSocketAddress address) throws IOException {
        return new HttpProxyImpl(authentication, address);
    }

    // Getters

    @NotNull ServerSocket getServer();

    /**
     * A proxy authentication can be used to allow only certain users. If the authentication is null, the user who makes a request using it will not need to provide the authentication details
     * @return the authentication object or null if none is required
     */
    @Nullable Authentication getAuthentication();

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
    default @NotNull HttpResponse request(@NotNull HttpRequest request) throws IOException, HttpException {
        @NotNull StringBuilder responseString = new StringBuilder();
        @NotNull StringBuilder requestString;

        try {
            // Serialize request
            @NotNull RequestLine line = request.getRequestLine();
            requestString = new StringBuilder(line.getMethod() + " " + line.getUri() + " " + line.getProtocolVersion());
            for (@NotNull Header header : request.getAllHeaders()) {
                requestString.append(header.getName()).append(": ").append(header.getValue());
            }
        } catch (@NotNull Throwable throwable) {
            throw new HttpException("cannot serialize request");
        }

        try (@NotNull Socket socket = new Socket(getHandle())) {
            socket.connect(new InetSocketAddress(request.getRequestLine().getUri(), 80));

            // Send website request
            @NotNull PrintWriter writer = new PrintWriter(socket.getOutputStream());
            writer.println(requestString);

            // Read request
            @NotNull BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            @NotNull String line;

            while ((line = reader.readLine()) != null) {
                responseString.append(line).append("\n");
            }

            writer.flush();
            writer.close();
        }

        try {
            @NotNull HttpResponse response = HttpUtils.parseResponse(responseString.toString());
            return response;
        } catch (@NotNull Throwable throwable) {
            throw new HttpException("invalid response format", throwable);
        }
    }

    // Loaders

    boolean start() throws Exception;

    boolean stop() throws Exception;

    // Classes

    /**
     * The authentication class is used to allow only users who provide some degree of authentication to use the proxy
     * @since 1.0-SNAPSHOT
     */
    interface Authentication {

        /**
         * Creates an authentication object that uses the bearer token scheme
         * <p style="color:red">Note: After the first successfullys validation, it removes the authorization header. It means if you try to validate again, it will return false.</p>
         *
         * @since 1.0-SNAPSHOT
         * @author Daniel Richard (Laivy)
         *
         * @see <a href="https://apidog.com/articles/what-is-bearer-token/">Bearer Authorization</a>
         * @param predicate a function that checks if the token is valid
         * @return an authentication object that implements the bearer token logic
         */
        static @NotNull Authentication bearer(@NotNull Predicate<String> predicate) {
            final @NotNull String headerName = "Proxy-Authorization";

            return (socket, request) -> {
                try {
                    if (!request.containsHeader(headerName)) {
                        return false;
                    }

                    @NotNull String[] auth = request.getLastHeader(headerName).getValue().split(" ");

                    if (auth.length < 2) {
                        return false;
                    } else if (auth[0].equalsIgnoreCase("Bearer")) {
                        return false;
                    }

                    return predicate.test(Arrays.stream(auth).skip(1).map(string -> string + " ").collect(Collectors.joining()));
                } catch (@NotNull Throwable ignore) {
                    return false;
                } finally {
                    request.removeHeaders(headerName);
                }
            };
        }

        /**
         * Creates an authentication object that uses the basic token scheme
         * <p style="color:red">Note: After the first successfully validation, it removes the authorization header. It means if you try to validate again, it will return false.</p>
         *
         * @since 1.0-SNAPSHOT
         * @author Daniel Richard (Laivy)
         *
         * @see <a href="https://en.wikipedia.org/wiki/Basic_access_authentication">Basic Authorization</a>
         * @param predicate a function that checks if the token is valid
         * @return an authentication object that implements the bearer token logic
         */
        static @NotNull Authentication basic(@NotNull Predicate<UsernamePasswordCredentials> predicate) {
            final @NotNull String headerName = "Proxy-Authorization";

            return (socket, request) -> {
                try {
                    if (!request.containsHeader(headerName)) {
                        return false;
                    }

                    @NotNull String[] auth = request.getLastHeader(headerName).getValue().split(" ");

                    if (auth.length < 2) {
                        return false;
                    } else if (auth[0].equalsIgnoreCase("Basic")) {
                        return false;
                    }

                    @NotNull String encoded = Arrays.stream(auth).skip(1).map(string -> string + " ").collect(Collectors.joining());
                    @NotNull String[] decoded = new String(Base64.getDecoder().decode(encoded)).split(":");

                    return predicate.test(new UsernamePasswordCredentials(decoded[0], decoded[1]));
                } catch (@NotNull Throwable ignore) {
                    return false;
                } finally {
                    request.removeHeaders(headerName);
                }
            };
        }

        /**
         * Validates the authentication by analyzing the socket and the request, returns true if the authentication was approved and the user is able to use the proxy or false otherwise. This method is invoked whenever a new request is made
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
