package codes.laivy.proxy.http;

import codes.laivy.proxy.JProxy;
import org.apache.http.HttpRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface HttpProxy extends JProxy {

    @Nullable Authentication getAuthentication();

    // Classes

    interface Authentication {

        static @NotNull Authentication bearer(@NotNull Predicate<String> predicate) {
            final @NotNull String headerName = "Proxy-Authorization";

            return request -> {
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
            };
        }

        boolean validate(@NotNull HttpRequest request);

    }

}
