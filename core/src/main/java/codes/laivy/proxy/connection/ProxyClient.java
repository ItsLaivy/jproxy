package codes.laivy.proxy.connection;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;

public interface ProxyClient {

    @NotNull Proxy getProxy();
    @NotNull InetSocketAddress getAddress();

    void close() throws IOException;

}
