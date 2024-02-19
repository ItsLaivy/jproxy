package codes.laivy.proxy.test.http;

import codes.laivy.proxy.http.HttpProxy;
import org.apache.hc.core5.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.junit.Assert;
import org.junit.Test;

import java.net.InetSocketAddress;

public final class HttpProxyTest {

    @Test
    public void connectInsecure() throws Throwable {
        // Start native http proxy
        @NotNull HttpProxy proxy = HttpProxy.create(null, new InetSocketAddress("localhost", 1250));
        Assert.assertTrue(proxy.start());

        // Test with JSoup
        @NotNull Connection connection = Jsoup.connect("http://laivy.cloud/")
                .proxy(proxy.getHandle())

                .ignoreContentType(true)
                .ignoreHttpErrors(true);
        @NotNull Connection.Response response = connection.execute();
        Assert.assertEquals(HttpStatus.SC_OK, response.statusCode());

        // End activities and stop
        Assert.assertTrue(proxy.stop());
    }

}
