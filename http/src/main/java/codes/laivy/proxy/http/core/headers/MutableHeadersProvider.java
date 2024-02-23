package codes.laivy.proxy.http.core.headers;

import codes.laivy.proxy.http.core.headers.Headers.MutableHeaders;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

final class MutableHeadersProvider implements MutableHeaders {

    private final @NotNull List<Header> list = new LinkedList<>();

    @Override
    public @NotNull Header @NotNull [] get(@NotNull String name) {
        return list.stream().filter(header -> header.getName().equalsIgnoreCase(name)).toArray(Header[]::new);
    }

    @Override
    public boolean contains(@NotNull String name) {
        return list.stream().anyMatch(header -> header.getName().equalsIgnoreCase(name));
    }

    @Override
    public @NotNull Stream<Header> stream() {
        return list.stream();
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean add(@NotNull Header header) {
        return list.add(header);
    }

    @Override
    public boolean remove(@NotNull Header header) {
        return list.remove(header);
    }

    @NotNull
    @Override
    public Iterator<Header> iterator() {
        return list.iterator();
    }
}
