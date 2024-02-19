import codes.laivy.proxy.http.HttpProxy;
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

}
