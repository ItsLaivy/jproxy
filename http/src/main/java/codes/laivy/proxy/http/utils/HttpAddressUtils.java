package codes.laivy.proxy.http.utils;

import codes.laivy.proxy.exception.SerializationException;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;

public final class HttpAddressUtils {

    private HttpAddressUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Retrieves an InetSocketAddress using a "Host" header.
     * If the host contains a port, it will be used; otherwise, the default port 80 is assumed.
     *
     * @author Daniel Richard (Laivy)
     * @since 1.0-SNAPSHOT
     *
     * @param host The host address, possibly including a port.
     * @return An InetSocketAddress representing the provided host.
     * @throws SerializationException Thrown to indicate that a string could not be parsed as a {@link InetSocketAddress} reference
     */
    public static @NotNull InetSocketAddress getAddressByHost(@NotNull String host) throws SerializationException {
        try {
            @NotNull String[] split = host.split(":");
            return new InetSocketAddress(split[0], host.contains(":") ? Integer.parseInt(split[1]) : 80);
        } catch (@NotNull Throwable throwable) {
            throw new SerializationException("cannot parse '" + host + "' as a valid socket address");
        }
    }


}
