import codes.laivy.proxy.http.HttpProxy;
import codes.laivy.proxy.http.core.HttpAuthorization;
import codes.laivy.proxy.http.core.HttpStatus;
import codes.laivy.proxy.http.core.headers.HeaderKey;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.*;

import java.net.InetSocketAddress;

// todo: exclusive http server for tests that verify headers and data
@TestMethodOrder(MethodOrderer.MethodName.class)
public final class HttpProxyTest {

    private static final @NotNull InetSocketAddress PROXY_LOCAL_ADDRESS = new InetSocketAddress("0.0.0.0", 55525);
    private static final @NotNull InetSocketAddress PROXY_EXTERNAL_ADDRESS = new InetSocketAddress("0.0.0.0", 55256);

    @Test
    public void connectReconnect() throws Throwable {
        // Start native http proxy
        try (@NotNull HttpProxy proxy = HttpProxy.create(PROXY_LOCAL_ADDRESS, null)) {
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
    @Order(1)
    public final class Insecure {
        @Test
        public void get() throws Throwable {
            // Start native http proxy
            try (@NotNull HttpProxy proxy = HttpProxy.create(PROXY_LOCAL_ADDRESS, null)) {
                Assertions.assertTrue(proxy.start());

                // Test with JSoup
                @NotNull Connection connection = Jsoup.connect("http://localhost/")
                        .proxy(proxy)

                        .method(Connection.Method.GET)

                        .ignoreContentType(true)
                        .ignoreHttpErrors(true);
                @NotNull Connection.Response response = connection.execute();
                Assertions.assertEquals(HttpStatus.OK.getCode(), response.statusCode(), response.statusMessage());

                // End activities and stop
                Assertions.assertTrue(proxy.stop());
            }
        }
        @Test
        public void post() throws Throwable {
            // Start native http proxy
            try (@NotNull HttpProxy proxy = HttpProxy.create(PROXY_LOCAL_ADDRESS, null)) {
                Assertions.assertTrue(proxy.start());

                // Json data
                @NotNull String data = "{\"test\":\"data\"}";
                // Test with JSoup
                @NotNull Connection connection = Jsoup.connect("http://localhost/")
                        .proxy(proxy)

                        .method(Connection.Method.POST)
                        .header(HeaderKey.CONTENT_TYPE.getName(), "application/json")
                        .requestBody(data)

                        .ignoreContentType(true)
                        .ignoreHttpErrors(true);
                @NotNull Connection.Response response = connection.execute();
                Assertions.assertEquals(HttpStatus.OK.getCode(), response.statusCode(), response.statusMessage());

                // End activities and stop
                Assertions.assertTrue(proxy.stop());
            }
        }
        @Test
        public void put() throws Throwable {
            // Start native http proxy
            try (@NotNull HttpProxy proxy = HttpProxy.create(PROXY_LOCAL_ADDRESS, null)) {
                Assertions.assertTrue(proxy.start());

                // Json data
                @NotNull String data = "{\"test\":\"data\"}";
                // Test with JSoup
                @NotNull Connection connection = Jsoup.connect("http://localhost/")
                        .proxy(proxy)

                        .method(Connection.Method.PUT)
                        .header(HeaderKey.CONTENT_TYPE.getName(), "application/json")
                        .requestBody(data)

                        .ignoreContentType(true)
                        .ignoreHttpErrors(true);
                @NotNull Connection.Response response = connection.execute();
                Assertions.assertEquals(HttpStatus.OK.getCode(), response.statusCode(), response.statusMessage());

                // End activities and stop
                Assertions.assertTrue(proxy.stop());
            }
        }
        @Test
        public void delete() throws Throwable {
            // Start native http proxy
            try (@NotNull HttpProxy proxy = HttpProxy.create(PROXY_LOCAL_ADDRESS, null)) {
                Assertions.assertTrue(proxy.start());

                // Test with JSoup
                @NotNull Connection connection = Jsoup.connect("http://localhost/")
                        .proxy(proxy)

                        .method(Connection.Method.DELETE)

                        .ignoreContentType(true)
                        .ignoreHttpErrors(true);
                @NotNull Connection.Response response = connection.execute();
                Assertions.assertEquals(HttpStatus.OK.getCode(), response.statusCode(), response.statusMessage());

                // End activities and stop
                Assertions.assertTrue(proxy.stop());
            }
        }
        @Test
        public void head() throws Throwable {
            // Start native http proxy
            try (@NotNull HttpProxy proxy = HttpProxy.create(PROXY_LOCAL_ADDRESS, null)) {
                Assertions.assertTrue(proxy.start());

                // Test with JSoup
                @NotNull Connection connection = Jsoup.connect("http://localhost/")
                        .proxy(proxy)

                        .method(Connection.Method.HEAD)

                        .ignoreContentType(true)
                        .ignoreHttpErrors(true);
                @NotNull Connection.Response response = connection.execute();
                Assertions.assertEquals(HttpStatus.OK.getCode(), response.statusCode(), response.statusMessage());

                // End activities and stop
                Assertions.assertTrue(proxy.stop());
            }
        }
        @Test
        public void options() throws Throwable {
            // Start native http proxy
            try (@NotNull HttpProxy proxy = HttpProxy.create(PROXY_LOCAL_ADDRESS, null)) {
                Assertions.assertTrue(proxy.start());

                // Test with JSoup
                @NotNull Connection connection = Jsoup.connect("http://localhost/")
                        .proxy(proxy)

                        .method(Connection.Method.OPTIONS)

                        .ignoreContentType(true)
                        .ignoreHttpErrors(true);
                @NotNull Connection.Response response = connection.execute();
                Assertions.assertEquals(HttpStatus.OK.getCode(), response.statusCode(), response.statusMessage());

                // End activities and stop
                Assertions.assertTrue(proxy.stop());
            }
        }

        @Test
        public void connectWithAuthorization() throws Throwable {
            @NotNull HeaderKey header = HeaderKey.PROXY_AUTHORIZATION;
            @NotNull String validToken = "valid_token_string";
            @NotNull String invalidToken = "invalid_token_string";
            Assertions.assertNotEquals(validToken, invalidToken);

            // Prepare connection and start http proxy
            @NotNull Connection connection;
            try (@NotNull HttpProxy proxy = HttpProxy.create(PROXY_LOCAL_ADDRESS, HttpAuthorization.bearer(header, (string) -> string.equals(validToken)))) {
                Assertions.assertTrue(proxy.start());

                // Test with JSoup without authorization
                connection = Jsoup.connect("http://localhost/").proxy(proxy).ignoreContentType(true).ignoreHttpErrors(true);
                Assertions.assertEquals(HttpStatus.PROXY_AUTHENTICATION_REQUIRED.getCode(), connection.execute().statusCode());
                // Test with JSoup with invalid authorization
                connection = Jsoup.connect("http://localhost/").proxy(proxy).header(header.getName(), "Bearer " + invalidToken).ignoreContentType(true).ignoreHttpErrors(true);
                Assertions.assertEquals(HttpStatus.UNAUTHORIZED.getCode(), connection.execute().statusCode());
                // Test with JSoup with valid authorization
                connection = Jsoup.connect("http://localhost/").proxy(proxy).header(header.getName(), "Bearer " + validToken).ignoreContentType(true).ignoreHttpErrors(true);
                Assertions.assertEquals(HttpStatus.OK.getCode(), connection.execute().statusCode());

                // End activities and stop
                Assertions.assertTrue(proxy.stop());
            }
        }

        @Test
        public void userInfo() throws Throwable {
            // Start native http proxy
            try (@NotNull HttpProxy proxy = HttpProxy.create(PROXY_LOCAL_ADDRESS, null)) {
                Assertions.assertTrue(proxy.start());

                // Test with JSoup
                @NotNull Connection connection = Jsoup.connect("http://username:password@localhost/")
                        .proxy(proxy)

                        .method(Connection.Method.GET)

                        .ignoreContentType(true)
                        .ignoreHttpErrors(true);
                @NotNull Connection.Response response = connection.execute();
                Assertions.assertEquals(HttpStatus.OK.getCode(), response.statusCode(), response.statusMessage());

                // End activities and stop
                Assertions.assertTrue(proxy.stop());
            }
        }

        @Test
        public void session() throws Throwable {
            // Start native http proxy
            try (@NotNull HttpProxy proxy = HttpProxy.create(PROXY_LOCAL_ADDRESS, null)) {
                Assertions.assertTrue(proxy.start());

                // Test with JSoup session
                @NotNull Connection connection = Jsoup.newSession()
                        .proxy(proxy)

                        .ignoreContentType(true)
                        .ignoreHttpErrors(true);
                @NotNull Connection.Response response;

                response = connection.newRequest("http://localhost/?test=1").execute();
                Assertions.assertEquals(HttpStatus.OK.getCode(), response.statusCode(), response.statusMessage());
                response = connection.newRequest("http://localhost/?test=2").execute();
                Assertions.assertEquals(HttpStatus.OK.getCode(), response.statusCode(), response.statusMessage());

                // End activities and stop
                Assertions.assertTrue(proxy.stop());
            }
        }
    }

    @Nested
    @Order(1)
    public final class Secure {
        @Test
        public void get() throws Throwable {
            // Start native http proxy
            try (@NotNull HttpProxy proxy = HttpProxy.create(PROXY_EXTERNAL_ADDRESS, null)) {
                Assertions.assertTrue(proxy.start());

                // Test with JSoup
                @NotNull Connection connection = Jsoup.connect("https://laivy.cloud/")
                        .proxy(proxy)

                        .method(Connection.Method.GET)

                        .ignoreContentType(true)
                        .ignoreHttpErrors(true);
                @NotNull Connection.Response response = connection.execute();
                Assertions.assertEquals(HttpStatus.OK.getCode(), response.statusCode(), response.statusMessage());

                // End activities and stop
                Assertions.assertTrue(proxy.stop());
            }
        }
    }
}
