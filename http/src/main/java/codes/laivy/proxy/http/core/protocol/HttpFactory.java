package codes.laivy.proxy.http.core.protocol;

import codes.laivy.proxy.http.connection.HttpProxyClient;
import codes.laivy.proxy.http.core.headers.Header;
import codes.laivy.proxy.http.core.request.HttpRequest;
import codes.laivy.proxy.http.core.response.HttpResponse;
import org.jetbrains.annotations.NotNull;

public interface HttpFactory {

    @NotNull Request getRequest();
    @NotNull Response getResponse();
    @NotNull Header getHeader();

    interface Request {
        @NotNull HttpRequest parse(@NotNull HttpProxyClient client, byte[] data);
        boolean isCompatible(@NotNull HttpProxyClient client, byte[] data);
    }
    interface Response {
        @NotNull HttpResponse parse(@NotNull HttpProxyClient client, byte[] data);
        boolean isCompatible(@NotNull HttpProxyClient client, byte[] data);
    }
    interface Headers {
        @NotNull Header parse(@NotNull HttpProxyClient client, byte[] data);
        boolean isCompatible(@NotNull HttpProxyClient client, byte[] data);
    }

}
