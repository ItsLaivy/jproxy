package codes.laivy.proxy.http.core.protocol;

import codes.laivy.proxy.http.core.protocol.v1_1.HttpVersion1_1;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.util.*;

public abstract class HttpVersion implements Closeable {

    // Static initializers

    @ApiStatus.Internal
    protected static final @NotNull Set<HttpVersion> versions = new TreeSet<>(Comparator.comparingInt(o -> (o.getMajor() + o.getMinor())));

    @SuppressWarnings("resource")
    public static @NotNull HttpVersion[] getVersions() {
        if (versions.stream().noneMatch(version -> version.getMajor() == 1 && version.getMinor() == 1)) {
            new HttpVersion1_1().init();
        }

        return versions.toArray(new HttpVersion[0]);
    }
    public static @NotNull HttpVersion getVersion(@NotNull String string) throws NullPointerException {
        @NotNull Optional<HttpVersion> optional = Arrays.stream(getVersions()).filter(version -> version.toString().equalsIgnoreCase(string)).findFirst();
        return optional.orElseThrow(() -> new NullPointerException("cannot find the HTTP version '" + string + "'"));
    }

    public static @NotNull HttpVersion HTTP1_1() {
        return Arrays.stream(getVersions()).filter(version -> version.getMajor() == 1 && version.getMinor() == 1).findFirst().orElseThrow(NullPointerException::new);
    }

    // Object

    private final int minor;

    private final int major;
    protected HttpVersion(int minor, int major) {
        this.minor = minor;
        this.major = major;

        getVersions();
    }

    // Modules

    public boolean init() {
        return versions.add(this);
    }

    @Override
    public void close() {
        versions.remove(this);
    }

    // Getters

    public abstract @NotNull HttpFactory getFactory();

    @Contract(pure = true)
    public final int getMinor() {
        return minor;
    }
    @Contract(pure = true)
    public final int getMajor() {
        return major;
    }

    // Implementations

    @Override
    public final boolean equals(@Nullable Object object) {
        if (this == object) return true;
        if (!(object instanceof HttpVersion)) return false;
        HttpVersion that = (HttpVersion) object;
        return getMinor() == that.getMinor() && getMajor() == that.getMajor();
    }
    @Override
    public final int hashCode() {
        return Objects.hash(getMinor(), getMajor());
    }

    @Override
    public @NotNull String toString() {
        return "HTTP/" + getMajor() + "." + getMinor();
    }

}
