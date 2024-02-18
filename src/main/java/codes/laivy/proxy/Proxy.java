package codes.laivy.proxy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;

public interface Proxy {

    @NotNull InetSocketAddress getAddress();

    @NotNull java.net.Proxy getHandle();

    // Natives

    @Override
    int hashCode();

    @Override
    boolean equals(@Nullable Object object);

}
