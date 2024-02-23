package codes.laivy.proxy.http.core.headers;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

final class ImmutableHeadersProvider implements Headers {

    private final @NotNull Header[] headers;

    public ImmutableHeadersProvider(@NotNull Header[] headers) {
        // todo: test this
        this.headers = Arrays.copyOf(headers, headers.length);
    }

    @Override
    public @NotNull Header @NotNull [] get(@NotNull String name) {
        return stream().filter(header -> header.getName().equalsIgnoreCase(name)).toArray(Header[]::new);
    }

    @Override
    public boolean contains(@NotNull String name) {
        return stream().anyMatch(header -> header.getName().equalsIgnoreCase(name));
    }

    @Override
    public @NotNull Stream<Header> stream() {
        return Arrays.stream(headers);
    }

    @Override
    public int size() {
        return headers.length;
    }

    @Override
    public @NotNull Iterator<Header> iterator() {
        return stream().iterator();
    }
}
