package codes.laivy.proxy.http.connection;

import codes.laivy.proxy.connection.ProxyClient;
import org.apache.hc.core5.http.HttpResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public interface HttpProxyClient extends ProxyClient {
    @Nullable InetSocketAddress getDestination();

    @NotNull Socket getSocket();

    boolean isSecure();
    void setSecure(boolean secure);

}
