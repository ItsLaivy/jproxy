package codes.laivy.proxy.http.core.headers;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.regex.Pattern;
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

    // Provided

    /**
     * @see <a href="regexr.com/7sfol">RegExr Tests</a>
     * @apiNote Last change: 23/02/2024 | 15:47 (GMT-3)
     */
    @NotNull HeaderKey ACCEPT = HeaderKey.create("Accept", Pattern.compile("^([a-zA-Z0-9+.*]+/[a-zA-Z0-9+.*]+(; ?q=[0-1](\\.[0-9]{1,2})?)?(, *)?)+$"));

    @NotNull HeaderKey ACCEPT_CH = HeaderKey.create("Accept-CH");
    @NotNull HeaderKey ACCEPT_CH_LIFETIME = HeaderKey.create("Accept-CH-Lifetime", Pattern.compile("^\\d+$"));

    /**
     * @see <a href="regexr.com/7sfpg">RegExr Tests</a>
     * @apiNote Last change: 23/02/2024 | 15:58 (GMT-3)
     */
    @NotNull HeaderKey ACCEPT_CHARSET = HeaderKey.create("Accept-Charset", Pattern.compile("^([\\w\\-]+(; ?q=[0-1]\\.?[0-9](\\.[0-9]{1,2})?)?(, *)?)+$"));

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

    interface MutableHeaders extends Headers {

        boolean add(@NotNull Header header);

        boolean remove(@NotNull Header header);

    }

}
