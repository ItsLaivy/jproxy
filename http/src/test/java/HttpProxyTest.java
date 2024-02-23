import codes.laivy.proxy.http.HttpProxy;
import codes.laivy.proxy.http.core.HttpAuthorization;
import codes.laivy.proxy.http.utils.HttpSerializers;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

// todo: exclusive http proxy server for tests that verify headers and data
// todo: tests with JSoup#newSession
@TestMethodOrder(MethodOrderer.MethodName.class)
public final class HttpProxyTest {

    private static final @NotNull InetSocketAddress PROXY_LOCAL_ADDRESS = new InetSocketAddress("localhost", 55525);
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
    @Order(0)
    public final class Serializers {
        @Nested
        public final class Request {
            @Test
            public void withoutBodyAndRequests() throws Throwable {
                for (@NotNull Connection.Method method : Connection.Method.values()) {
                    try {
                        @NotNull HttpRequest basic = new BasicHttpRequest(method.name(), new HttpHost(InetAddress.getByName("laivy.cloud"), 443), "/");
                        basic.setVersion(HttpVersion.HTTP_1_1);

                        @NotNull ByteBuffer serialized = HttpSerializers.getHttpRequest().serialize(basic);
                        @NotNull HttpRequest deserialized = HttpSerializers.getHttpRequest().deserialize(serialized);

                        Assertions.assertEquals(new String(serialized.array()), new String(HttpSerializers.getHttpRequest().serialize(deserialized).array()));
                    } catch (@NotNull Throwable throwable) {
                        throw new Throwable(method.name(), throwable);
                    }
                }
            }
            @Test
            public void withoutBody() throws Throwable {
                for (@NotNull Connection.Method method : Connection.Method.values()) {
                    try {
                        @NotNull HttpRequest basic = new BasicHttpRequest(method.name(), new HttpHost(InetAddress.getByName("laivy.cloud"), 443), "/");
                        basic.setVersion(HttpVersion.HTTP_1_1);

                        basic.addHeader(HttpHeaders.HOST, "laivy.cloud:443");
                        basic.addHeader(HttpHeaders.PROXY_AUTHORIZATION, "Bearer Test");
                        basic.addHeader(HttpHeaders.CONNECTION, "keep-alive");

                        @NotNull ByteBuffer serialized = HttpSerializers.getHttpRequest().serialize(basic);
                        @NotNull HttpRequest deserialized = HttpSerializers.getHttpRequest().deserialize(serialized);

                        Assertions.assertEquals(new String(serialized.array()), new String(HttpSerializers.getHttpRequest().serialize(deserialized).array()));
                    } catch (@NotNull Throwable throwable) {
                        throw new Throwable(method.name(), throwable);
                    }
                }
            }
            @Test
            public void withBody() throws Throwable {
                for (@NotNull Connection.Method method : Connection.Method.values()) {
                    try {
                        @NotNull BasicClassicHttpRequest basic = new BasicClassicHttpRequest(method.name(), new HttpHost(InetAddress.getByName("laivy.cloud"), 443), "/");
                        basic.setVersion(HttpVersion.HTTP_1_1);
                        basic.setEntity(new StringEntity("Cool"));

                        basic.addHeader(HttpHeaders.HOST, "laivy.cloud:443");
                        basic.addHeader(HttpHeaders.PROXY_AUTHORIZATION, "Bearer Test");
                        basic.addHeader(HttpHeaders.CONNECTION, "keep-alive");

                        @NotNull ByteBuffer serialized = HttpSerializers.getHttpRequest().serialize(basic);
                        @NotNull HttpRequest deserialized = HttpSerializers.getHttpRequest().deserialize(serialized);

                        Assertions.assertEquals(new String(serialized.array()), new String(HttpSerializers.getHttpRequest().serialize(deserialized).array()));
                    } catch (@NotNull Throwable throwable) {
                        throw new Throwable(method.name(), throwable);
                    }
                }
            }
        }
        @Nested
        public final class Response {
            @Test
            public void withoutBodyAndRequests() throws Throwable {
                @NotNull HttpResponse basic = new BasicHttpResponse(200, "OK");
                basic.setVersion(HttpVersion.HTTP_1_1);

                @NotNull ByteBuffer serialized = HttpSerializers.getHttpResponse().serialize(basic);
                @NotNull HttpResponse deserialized = HttpSerializers.getHttpResponse().deserialize(serialized);

                Assertions.assertEquals(new String(serialized.array()), new String(HttpSerializers.getHttpResponse().serialize(deserialized).array()));
            }
            @Test
            public void withoutBody() throws Throwable {
                @NotNull HttpResponse basic = new BasicHttpResponse(200, "OK");
                basic.setVersion(HttpVersion.HTTP_1_1);

                basic.addHeader(HttpHeaders.HOST, "laivy.cloud:443");
                basic.addHeader(HttpHeaders.PROXY_AUTHORIZATION, "Bearer Test");
                basic.addHeader(HttpHeaders.CONNECTION, "keep-alive");

                @NotNull ByteBuffer serialized = HttpSerializers.getHttpResponse().serialize(basic);
                @NotNull HttpResponse deserialized = HttpSerializers.getHttpResponse().deserialize(serialized);

                Assertions.assertEquals(new String(serialized.array()), new String(HttpSerializers.getHttpResponse().serialize(deserialized).array()));
            }
            @Test
            public void withBody() throws Throwable {
                @NotNull BasicClassicHttpResponse basic = new BasicClassicHttpResponse(200, "OK");
                basic.setVersion(HttpVersion.HTTP_1_1);
                basic.setEntity(new StringEntity("Cool"));

                basic.addHeader(HttpHeaders.HOST, "laivy.cloud:443");
                basic.addHeader(HttpHeaders.PROXY_AUTHORIZATION, "Bearer Test");
                basic.addHeader(HttpHeaders.CONNECTION, "keep-alive");

                @NotNull ByteBuffer serialized = HttpSerializers.getHttpResponse().serialize(basic);
                @NotNull HttpResponse deserialized = HttpSerializers.getHttpResponse().deserialize(serialized);

                Assertions.assertEquals(new String(serialized.array()), new String(HttpSerializers.getHttpResponse().serialize(deserialized).array()));
            }
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
                Assertions.assertEquals(HttpStatus.SC_OK, response.statusCode(), response.statusMessage());

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
            try (@NotNull HttpProxy proxy = HttpProxy.create(PROXY_LOCAL_ADDRESS, null)) {
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
            try (@NotNull HttpProxy proxy = HttpProxy.create(PROXY_LOCAL_ADDRESS, null)) {
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
            try (@NotNull HttpProxy proxy = HttpProxy.create(PROXY_LOCAL_ADDRESS, null)) {
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
            try (@NotNull HttpProxy proxy = HttpProxy.create(PROXY_LOCAL_ADDRESS, null)) {
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
            try (@NotNull HttpProxy proxy = HttpProxy.create(PROXY_LOCAL_ADDRESS, HttpAuthorization.bearer(headerName, (string) -> string.equals(validToken)))) {
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
            try (@NotNull HttpProxy proxy = HttpProxy.create(PROXY_LOCAL_ADDRESS, null)) {
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
    @Order(2)
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
                Assertions.assertEquals(HttpStatus.SC_OK, response.statusCode(), response.statusMessage());

                // End activities and stop
                Assertions.assertTrue(proxy.stop());
            }
        }
    }

}
