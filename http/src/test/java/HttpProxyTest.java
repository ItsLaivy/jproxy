import codes.laivy.proxy.http.HttpProxy;
import codes.laivy.proxy.http.HttpProxy.Authorization;
import org.apache.hc.core5.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.junit.Assert;
import org.junit.Test;

import java.net.InetSocketAddress;

public final class HttpProxyTest {

    private static final @NotNull InetSocketAddress PROXY_ADDRESS = new InetSocketAddress("localhost", 5555);

    @Test
    public void connectReconnect() throws Throwable {
        // Start native http proxy
        @NotNull HttpProxy proxy = HttpProxy.create(PROXY_ADDRESS, null);
        Assert.assertTrue(proxy.start());

        // End activities and stop
        Assert.assertTrue(proxy.stop());
        // Start activities again
        Assert.assertTrue(proxy.start());
        // Finally end activities and stop without starting again
        Assert.assertTrue(proxy.stop());
    }

    @Test
    public void connectInsecure() throws Throwable {
        // Start native http proxy
        @NotNull HttpProxy proxy = HttpProxy.create(PROXY_ADDRESS, null);
        Assert.assertTrue(proxy.start());

        // Test with JSoup
        @NotNull Connection connection = Jsoup.connect("http://localhost/")
                .proxy(proxy)

                .ignoreContentType(true)
                .ignoreHttpErrors(true);
        @NotNull Connection.Response response = connection.execute();
        Assert.assertEquals(HttpStatus.SC_OK, response.statusCode());

        // End activities and stop
        Assert.assertTrue(proxy.stop());
    }

    @Test
    public void connectInsecureWithAuthorization() throws Throwable {
        @NotNull String headerName = "Proxy-Authorization";
        @NotNull String validToken = "valid_token_string";
        @NotNull String invalidToken = "invalid_token_string";
        Assert.assertNotEquals(validToken, invalidToken);

        // Prepare connection and start http proxy
        @NotNull Connection connection;
        @NotNull HttpProxy proxy = HttpProxy.create(PROXY_ADDRESS, Authorization.bearer(headerName, string -> string.equals(validToken)));
        Assert.assertTrue(proxy.start());

        // Test with JSoup without authorization
        connection = Jsoup.connect("http://localhost/").proxy(proxy).ignoreContentType(true).ignoreHttpErrors(true);
        Assert.assertEquals(HttpStatus.SC_UNAUTHORIZED, connection.execute().statusCode());
        // Test with JSoup with invalid authorization
        connection = Jsoup.connect("http://localhost/").proxy(proxy).header(headerName, "Bearer " + invalidToken).ignoreContentType(true).ignoreHttpErrors(true);
        Assert.assertEquals(HttpStatus.SC_UNAUTHORIZED, connection.execute().statusCode());
        // Test with JSoup with valid authorization
        connection = Jsoup.connect("http://localhost/").proxy(proxy).header(headerName, "Bearer " + validToken).ignoreContentType(true).ignoreHttpErrors(true);
        Assert.assertEquals(HttpStatus.SC_OK, connection.execute().statusCode());

        // End activities and stop
        Assert.assertTrue(proxy.stop());
    }

}
