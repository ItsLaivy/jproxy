package codes.laivy.proxy.http.core.protocol;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public abstract class HttpVersion implements Closeable {

    // Static initializers

    @ApiStatus.Internal
    protected static final @NotNull Set<HttpVersion> versions = new TreeSet<>(Comparator.comparingInt(o -> (o.getMajor() + o.getMinor())));

    public static @NotNull HttpVersion[] getVersions() {
        return versions.toArray(new HttpVersion[0]);
    }

    // Object

    private final int minor;
    private final int major;

    protected HttpVersion(int minor, int major) {
        this.minor = minor;
        this.major = major;
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
