package codes.laivy.proxy.http.core.protocol.v1_1;

import codes.laivy.proxy.http.core.protocol.HttpFactory;
import codes.laivy.proxy.http.core.protocol.HttpVersion;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class HttpVersion1_1 extends HttpVersion {

    private final @NotNull HttpFactory factory = new HttpFactory1_1(this);

    public HttpVersion1_1() {
        super(1, 1);
    }

    @Override
    public @NotNull HttpFactory getFactory() {
        return factory;
    }

}
