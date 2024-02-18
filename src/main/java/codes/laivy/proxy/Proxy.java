package codes.laivy.proxy;

import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public interface Proxy {

    @NotNull InetSocketAddress getAddress();

    @NotNull java.net.Proxy getHandle();

}
