package codes.laivy.proxy;

import codes.laivy.proxy.connection.ProxyClient;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Collection;

public abstract class ProxyServer extends Proxy {

    private final @NotNull InetSocketAddress address;
    private final @NotNull Type type;

    protected ProxyServer(@NotNull Type type, @NotNull InetSocketAddress address) {
        super(type, new InetSocketAddress("localhost", 0));

        this.address = address;
        this.type = type;
    }

    // Getters

    @Override
    public final @NotNull InetSocketAddress address() {
        return this.address;
    }
    @Override
    public Type type() {
        return this.type;
    }

    // Modules

    public abstract @NotNull Collection<? extends ProxyClient> getClients();

}
