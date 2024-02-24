package codes.laivy.proxy.http.core.protocol;

import codes.laivy.proxy.http.connection.HttpProxyClient;
import codes.laivy.proxy.http.core.headers.Header;
import codes.laivy.proxy.http.core.request.HttpRequest;
import codes.laivy.proxy.http.core.response.HttpResponse;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;

public interface HttpFactory {

    @NotNull Request getRequest();
    @NotNull Response getResponse();
    @NotNull Headers getHeaders();

    interface Request {
        @NotNull HttpRequest parse(@NotNull HttpProxyClient client, byte[] data) throws ParseException;
        byte[] wrap(@NotNull HttpRequest request);

        boolean isCompatible(@NotNull HttpProxyClient client, byte[] data);
    }
    interface Response {
        @NotNull HttpResponse parse(@NotNull HttpProxyClient client, byte[] data) throws ParseException;
        byte[] wrap(@NotNull HttpResponse response);

        boolean isCompatible(@NotNull HttpProxyClient client, byte[] data);
    }
    interface Headers {
        @NotNull Header parse(byte[] data) throws ParseException;
        byte[] wrap(@NotNull Header header);

        boolean isCompatible(byte[] data);
    }

}
