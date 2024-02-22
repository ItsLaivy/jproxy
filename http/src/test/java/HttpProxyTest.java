import codes.laivy.proxy.http.HttpProxy;
import codes.laivy.proxy.http.core.HttpAuthorization;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

// todo: exclusive http proxy server for tests that verify headers and data
// todo: tests with JSoup#newSession
public final class HttpProxyTest {

    private static final @NotNull InetSocketAddress PROXY_ADDRESS = new InetSocketAddress("localhost", 5555);

    @Test
    public void connectReconnect() throws Throwable {
        // Start native http proxy
        try (@NotNull HttpProxy proxy = HttpProxy.create(PROXY_ADDRESS, null)) {
            Assertions.assertTrue(proxy.start());

            // End activities and stop
            Assertions.assertTrue(proxy.stop());
            // Start activities again
            Assertions.assertTrue(proxy.start());
            // Finally end activities and stop without starting again
            Assertions.assertTrue(proxy.stop());
        }
    }

    @Nested
    public final class Insecure {
        @Test
        public void get() throws Throwable {
            // Start native http proxy
            try (@NotNull HttpProxy proxy = HttpProxy.create(PROXY_ADDRESS, null)) {
                Assertions.assertTrue(proxy.start());

                // Test with JSoup
                @NotNull Connection connection = Jsoup.connect("http://localhost/")
                        .proxy(proxy)

                        .method(Connection.Method.GET)

                        .ignoreContentType(true)
                        .ignoreHttpErrors(true);
                @NotNull Connection.Response response = connection.execute();
                Assertions.assertEquals(HttpStatus.SC_OK, response.statusCode(), response.statusMessage());

                // End activities and stop
                Assertions.assertTrue(proxy.stop());
            }
        }
        @Test
        public void post() throws Throwable {
            // Start native http proxy
            try (@NotNull HttpProxy proxy = HttpProxy.create(PROXY_ADDRESS, null)) {
                Assertions.assertTrue(proxy.start());

                // Json data
                @NotNull String data = "{\"test\":\"data\"}";
                // Test with JSoup
                @NotNull Connection connection = Jsoup.connect("http://localhost/")
                        .proxy(proxy)

                        .method(Connection.Method.POST)
                        .header(HttpHeaders.CONTENT_TYPE, "application/json")
                        .requestBody(data)

                        .ignoreContentType(true)
                        .ignoreHttpErrors(true);
                @NotNull Connection.Response response = connection.execute();
                Assertions.assertEquals(HttpStatus.SC_OK, response.statusCode(), response.statusMessage());

                // End activities and stop
                Assertions.assertTrue(proxy.stop());
            }
        }
        @Test
        public void put() throws Throwable {
            // Start native http proxy
            try (@NotNull HttpProxy proxy = HttpProxy.create(PROXY_ADDRESS, null)) {
                Assertions.assertTrue(proxy.start());

                // Json data
                @NotNull String data = "{\"test\":\"data\"}";
                // Test with JSoup
                @NotNull Connection connection = Jsoup.connect("http://localhost/")
                        .proxy(proxy)

                        .method(Connection.Method.PUT)
                        .header(HttpHeaders.CONTENT_TYPE, "application/json")
                        .requestBody(data)

                        .ignoreContentType(true)
                        .ignoreHttpErrors(true);
                @NotNull Connection.Response response = connection.execute();
                Assertions.assertEquals(HttpStatus.SC_OK, response.statusCode(), response.statusMessage());

                // End activities and stop
                Assertions.assertTrue(proxy.stop());
            }
        }
        @Test
        public void delete() throws Throwable {
            // Start native http proxy
            try (@NotNull HttpProxy proxy = HttpProxy.create(PROXY_ADDRESS, null)) {
                Assertions.assertTrue(proxy.start());

                // Test with JSoup
                @NotNull Connection connection = Jsoup.connect("http://localhost/")
                        .proxy(proxy)

                        .method(Connection.Method.DELETE)

                        .ignoreContentType(true)
                        .ignoreHttpErrors(true);
                @NotNull Connection.Response response = connection.execute();
                Assertions.assertEquals(HttpStatus.SC_OK, response.statusCode(), response.statusMessage());

                // End activities and stop
                Assertions.assertTrue(proxy.stop());
            }
        }
        @Test
        public void head() throws Throwable {
            // Start native http proxy
            try (@NotNull HttpProxy proxy = HttpProxy.create(PROXY_ADDRESS, null)) {
                Assertions.assertTrue(proxy.start());

                // Test with JSoup
                @NotNull Connection connection = Jsoup.connect("http://localhost/")
                        .proxy(proxy)

                        .method(Connection.Method.HEAD)

                        .ignoreContentType(true)
                        .ignoreHttpErrors(true);
                @NotNull Connection.Response response = connection.execute();
                Assertions.assertEquals(HttpStatus.SC_OK, response.statusCode(), response.statusMessage());

                // End activities and stop
                Assertions.assertTrue(proxy.stop());
            }
        }
        @Test
        public void options() throws Throwable {
            // Start native http proxy
            try (@NotNull HttpProxy proxy = HttpProxy.create(PROXY_ADDRESS, null)) {
                Assertions.assertTrue(proxy.start());

                // Test with JSoup
                @NotNull Connection connection = Jsoup.connect("http://localhost/")
                        .proxy(proxy)

                        .method(Connection.Method.OPTIONS)

                        .ignoreContentType(true)
                        .ignoreHttpErrors(true);
                @NotNull Connection.Response response = connection.execute();
                Assertions.assertEquals(HttpStatus.SC_OK, response.statusCode(), response.statusMessage());

                // End activities and stop
                Assertions.assertTrue(proxy.stop());
            }
        }

        @Test
        public void connectWithAuthorization() throws Throwable {
            @NotNull String headerName = HttpHeaders.PROXY_AUTHORIZATION;
            @NotNull String validToken = "valid_token_string";
            @NotNull String invalidToken = "invalid_token_string";
            Assertions.assertNotEquals(validToken, invalidToken);

            // Prepare connection and start http proxy
            @NotNull Connection connection;
            try (@NotNull HttpProxy proxy = HttpProxy.create(PROXY_ADDRESS, HttpAuthorization.bearer(headerName, (string) -> string.equals(validToken)))) {
                Assertions.assertTrue(proxy.start());

                // Test with JSoup without authorization
                connection = Jsoup.connect("http://localhost/").proxy(proxy).ignoreContentType(true).ignoreHttpErrors(true);
                Assertions.assertEquals(HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED, connection.execute().statusCode());
                // Test with JSoup with invalid authorization
                connection = Jsoup.connect("http://localhost/").proxy(proxy).header(headerName, "Bearer " + invalidToken).ignoreContentType(true).ignoreHttpErrors(true);
                Assertions.assertEquals(HttpStatus.SC_UNAUTHORIZED, connection.execute().statusCode());
                // Test with JSoup with valid authorization
                connection = Jsoup.connect("http://localhost/").proxy(proxy).header(headerName, "Bearer " + validToken).ignoreContentType(true).ignoreHttpErrors(true);
                Assertions.assertEquals(HttpStatus.SC_OK, connection.execute().statusCode());

                // End activities and stop
                Assertions.assertTrue(proxy.stop());
            }
        }

        @Test
        public void session() throws Throwable {
            // Start native http proxy
            try (@NotNull HttpProxy proxy = HttpProxy.create(PROXY_ADDRESS, null)) {
                Assertions.assertTrue(proxy.start());

                // Test with JSoup session
                @NotNull Connection connection = Jsoup.newSession()
                        .proxy(proxy)
                        .ignoreContentType(true)
                        .ignoreHttpErrors(true);
                @NotNull Connection.Response response;

                response = connection.newRequest("http://localhost/?test=1").execute();
                Assertions.assertEquals(HttpStatus.SC_OK, response.statusCode(), response.statusMessage());
                response = connection.newRequest("http://localhost/?test=2").execute();
                Assertions.assertEquals(HttpStatus.SC_OK, response.statusCode(), response.statusMessage());

                // End activities and stop
                Assertions.assertTrue(proxy.stop());
            }
        }
    }

    @Nested
    public final class Secure {
        @Test
        public void get() throws Throwable {
            // Start native http proxy
            try (@NotNull HttpProxy proxy = HttpProxy.create(PROXY_ADDRESS, null)) {
                Assertions.assertTrue(proxy.start());

                // Test with JSoup
                @NotNull Connection connection = Jsoup.connect("https://laivy.cloud/")
                        .proxy(proxy)

                        .method(Connection.Method.GET)

                        .ignoreContentType(true)
                        .ignoreHttpErrors(true);
                @NotNull Connection.Response response = connection.execute();
                Assertions.assertEquals(HttpStatus.SC_OK, response.statusCode(), response.statusMessage());

                // End activities and stop
                Assertions.assertTrue(proxy.stop());
            }
        }
    }

}
