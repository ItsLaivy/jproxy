package codes.laivy.proxy.http.core;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.ProtocolVersion;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Locale;

public final class SecureHttpResponse implements HttpResponse {

    private final byte @NotNull [] data;

    public SecureHttpResponse(byte @NotNull [] data) {
        this.data = data;
    }

    public byte @NotNull [] getData() {
        return data;
    }

    @Override
    public ProtocolVersion getVersion() {
        throw new UnsupportedOperationException("the protocol version of a secure http response cannot be retrieved");
    }

    @Override
    public void setVersion(ProtocolVersion version) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addHeader(Header header) {
        throw new UnsupportedOperationException("the header of a secure http response cannot be retrieved/changed");
    }

    @Override
    public void addHeader(String name, Object value) {
        throw new UnsupportedOperationException("the header of a secure http response cannot be retrieved/changed");
    }

    @Override
    public void setHeader(Header header) {
        throw new UnsupportedOperationException("the header of a secure http response cannot be retrieved/changed");
    }

    @Override
    public void setHeader(String name, Object value) {
        throw new UnsupportedOperationException("the header of a secure http response cannot be retrieved/changed");
    }

    @Override
    public void setHeaders(Header... headers) {
        throw new UnsupportedOperationException("the header of a secure http response cannot be retrieved/changed");
    }

    @Override
    public boolean removeHeader(Header header) {
        throw new UnsupportedOperationException("the header of a secure http response cannot be retrieved/changed");
    }

    @Override
    public boolean removeHeaders(String name) {
        throw new UnsupportedOperationException("the header of a secure http response cannot be retrieved/changed");
    }

    @Override
    public boolean containsHeader(String name) {
        throw new UnsupportedOperationException("the header of a secure http response cannot be retrieved/changed");
    }

    @Override
    public int countHeaders(String name) {
        throw new UnsupportedOperationException("the header of a secure http response cannot be retrieved/changed");
    }

    @Override
    public Header getFirstHeader(String name) {
        throw new UnsupportedOperationException("the header of a secure http response cannot be retrieved/changed");
    }

    @Override
    public Header getHeader(String name) throws ProtocolException {
        throw new UnsupportedOperationException("the header of a secure http response cannot be retrieved/changed");
    }

    @Override
    public Header[] getHeaders() {
        throw new UnsupportedOperationException("the header of a secure http response cannot be retrieved/changed");
    }

    @Override
    public Header[] getHeaders(String name) {
        throw new UnsupportedOperationException("the header of a secure http response cannot be retrieved/changed");
    }

    @Override
    public Header getLastHeader(String name) {
        throw new UnsupportedOperationException("the header of a secure http response cannot be retrieved/changed");
    }

    @Override
    public Iterator<Header> headerIterator() {
        throw new UnsupportedOperationException("the header of a secure http response cannot be retrieved/changed");
    }

    @Override
    public Iterator<Header> headerIterator(String name) {
        throw new UnsupportedOperationException("the header of a secure http response cannot be retrieved/changed");
    }

    @Override
    public int getCode() {
        throw new UnsupportedOperationException("the code of a secure http response cannot be retrieved/changed");
    }

    @Override
    public void setCode(int code) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getReasonPhrase() {
        throw new UnsupportedOperationException("the message of a secure http response cannot be retrieved/changed");
    }

    @Override
    public void setReasonPhrase(String reason) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Locale getLocale() {
        throw new UnsupportedOperationException("the locale of a secure http response cannot be retrieved/changed");
    }

    @Override
    public void setLocale(Locale loc) {
        throw new UnsupportedOperationException();
    }
}
