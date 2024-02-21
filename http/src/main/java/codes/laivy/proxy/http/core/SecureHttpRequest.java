package codes.laivy.proxy.http.core;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.net.URIAuthority;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

public final class SecureHttpRequest implements HttpRequest {

    private final byte @NotNull [] data;

    public SecureHttpRequest(byte @NotNull [] data) {
        this.data = data;
    }

    public byte @NotNull [] getData() {
        return data;
    }

    @Override
    public @NotNull String getMethod() {
        throw new UnsupportedOperationException("the method of a secure http request cannot be retrieved");
    }

    @Override
    public String getPath() {
        throw new UnsupportedOperationException("the path of a secure http request cannot be retrieved");
    }

    @Override
    public void setPath(String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getScheme() {
        throw new UnsupportedOperationException("the scheme of a secure http request cannot be retrieved");
    }

    @Override
    public void setScheme(String scheme) {
        throw new UnsupportedOperationException();
    }

    @Override
    public URIAuthority getAuthority() {
        throw new UnsupportedOperationException("the authority of a secure http request cannot be retrieved");
    }

    @Override
    public void setAuthority(URIAuthority authority) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRequestUri() {
        throw new UnsupportedOperationException("the request uri of a secure http request cannot be retrieved");
    }

    @Override
    public URI getUri() throws URISyntaxException {
        throw new UnsupportedOperationException("the uri of a secure http request cannot be retrieved");
    }

    @Override
    public void setUri(URI requestUri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProtocolVersion getVersion() {
        throw new UnsupportedOperationException("the protocol version of a secure http request cannot be retrieved");
    }

    @Override
    public void setVersion(ProtocolVersion version) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addHeader(Header header) {
        throw new UnsupportedOperationException("the header of a secure http request cannot be retrieved/changed");
    }

    @Override
    public void addHeader(String name, Object value) {
        throw new UnsupportedOperationException("the header of a secure http request cannot be retrieved/changed");
    }

    @Override
    public void setHeader(Header header) {
        throw new UnsupportedOperationException("the header of a secure http request cannot be retrieved/changed");
    }

    @Override
    public void setHeader(String name, Object value) {
        throw new UnsupportedOperationException("the header of a secure http request cannot be retrieved/changed");
    }

    @Override
    public void setHeaders(Header... headers) {
        throw new UnsupportedOperationException("the header of a secure http request cannot be retrieved/changed");
    }

    @Override
    public boolean removeHeader(Header header) {
        throw new UnsupportedOperationException("the header of a secure http request cannot be retrieved/changed");
    }

    @Override
    public boolean removeHeaders(String name) {
        throw new UnsupportedOperationException("the header of a secure http request cannot be retrieved/changed");
    }

    @Override
    public boolean containsHeader(String name) {
        throw new UnsupportedOperationException("the header of a secure http request cannot be retrieved/changed");
    }

    @Override
    public int countHeaders(String name) {
        throw new UnsupportedOperationException("the header of a secure http request cannot be retrieved/changed");
    }

    @Override
    public Header getFirstHeader(String name) {
        throw new UnsupportedOperationException("the header of a secure http request cannot be retrieved/changed");
    }

    @Override
    public Header getHeader(String name) throws ProtocolException {
        throw new UnsupportedOperationException("the header of a secure http request cannot be retrieved/changed");
    }

    @Override
    public Header[] getHeaders() {
        throw new UnsupportedOperationException("the header of a secure http request cannot be retrieved/changed");
    }

    @Override
    public Header[] getHeaders(String name) {
        throw new UnsupportedOperationException("the header of a secure http request cannot be retrieved/changed");
    }

    @Override
    public Header getLastHeader(String name) {
        throw new UnsupportedOperationException("the header of a secure http request cannot be retrieved/changed");
    }

    @Override
    public Iterator<Header> headerIterator() {
        throw new UnsupportedOperationException("the header of a secure http request cannot be retrieved/changed");
    }

    @Override
    public Iterator<Header> headerIterator(String name) {
        throw new UnsupportedOperationException("the header of a secure http request cannot be retrieved/changed");
    }
}
