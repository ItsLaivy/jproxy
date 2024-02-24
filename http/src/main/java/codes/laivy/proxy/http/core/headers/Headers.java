package codes.laivy.proxy.http.core.headers;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Headers extends Iterable<Header> {

    // Static initializers

    static @NotNull MutableHeaders createMutable() {
        return new MutableHeadersProvider();
    }
    static @NotNull Headers createImmutable(@NotNull Header @NotNull [] headers) {
        return new ImmutableHeadersProvider(headers);
    }

    // Object

    @NotNull Header @NotNull [] get(@NotNull String name);
    boolean contains(@NotNull String name);

    @NotNull Stream<Header> stream();

    int size();

    default int count(@NotNull String name) {
        return (int) stream().filter(header -> header.getName().equalsIgnoreCase(name)).count();
    }

    default @NotNull Optional<Header> first(@NotNull String name) {
        return stream().filter(header -> header.getName().equalsIgnoreCase(name)).findFirst();
    }
    default @NotNull Optional<Header> last(@NotNull String name) {
        return stream()
                .filter(header -> header.getName().equalsIgnoreCase(name))
                .collect(Collectors.toList())
                .stream()
                .reduce((first, second) -> second);
    }

    default boolean contains(@NotNull HeaderKey key) {
        return contains(key.getName());
    }
    default int count(@NotNull HeaderKey key) {
        return count(key.getName());
    }

    default @NotNull Optional<Header> first(@NotNull HeaderKey key) {
        return first(key.getName());
    }
    default @NotNull Optional<Header> last(@NotNull HeaderKey key) {
        return last(key.getName());
    }

    default @NotNull Header @NotNull [] get(@NotNull HeaderKey key) {
        return get(key.getName());
    }


    interface MutableHeaders extends Headers {

        boolean add(@NotNull Header header);

        boolean remove(@NotNull Header header);

    }

}
