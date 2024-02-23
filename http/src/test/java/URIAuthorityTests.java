import codes.laivy.proxy.http.core.Credentials.Basic;
import codes.laivy.proxy.http.core.URIAuthority;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.IDN;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

public final class URIAuthorityTests {

    private static final class Meta {

        private final @NotNull String input;

        private final @Nullable Basic userInfo;
        private final @NotNull String hostName;
        private final int port;

        public Meta(@NotNull String input, @Nullable Basic userInfo, @NotNull String hostName, int port) {
            this.input = input;
            this.userInfo = userInfo;
            this.hostName = hostName;
            this.port = port;
        }
    }

    @Test
    public void parser() throws UnknownHostException, URISyntaxException {
        @NotNull Meta[] metas = new Meta[] {
                new Meta("https://localhost/", null, "localhost", URIAuthority.DEFAULT_HTTPS_PORT),
                new Meta("https://user:pass@localhost/", new Basic("user", "pass".toCharArray()), "localhost", URIAuthority.DEFAULT_HTTPS_PORT),
                new Meta("https://user:pass@localhost:555/", new Basic("user", "pass".toCharArray()), "localhost", 555),
                new Meta("https://user:pass@laivy.cloud:555/", new Basic("user", "pass".toCharArray()), "laivy.cloud", 555),
                new Meta("https://user:pass@laivy.cloud:555/test.php", new Basic("user", "pass".toCharArray()), "laivy.cloud", 555),
                new Meta("localhost:232", null, "localhost", 232),
                new Meta("localhost:232/", null, "localhost", 232),
                new Meta("https://localhost:444/test.php", null, "localhost", 444),
                new Meta("https://127.0.0.1/", null, "127.0.0.1", URIAuthority.DEFAULT_HTTPS_PORT),
                new Meta("münchen.de", null, IDN.toASCII("münchen.de"), URIAuthority.DEFAULT_HTTP_PORT),
                new Meta("localhost", null, "localhost", URIAuthority.DEFAULT_HTTP_PORT),
                new Meta("http://localhost/", null, "localhost", URIAuthority.DEFAULT_HTTP_PORT),
                new Meta("http://localhost:502/", null, "localhost", 502),
        };

        for (@NotNull Meta meta : metas) {
            @NotNull URIAuthority authority = URIAuthority.parse(meta.input);

            Assertions.assertEquals(authority.getUserInfo(), meta.userInfo, meta.input);
            Assertions.assertEquals(authority.getHostName(), meta.hostName, meta.input);
            Assertions.assertEquals(authority.getPort(), meta.port, meta.input);
        }
    }

}