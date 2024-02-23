package codes.laivy.proxy.http.core;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.Flushable;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Objects;

public interface Credentials extends CharSequence, Flushable {

    byte[] getBytes();

    @Override
    default int length() {
        return getBytes().length;
    }

    @Override
    default char charAt(int index) {
        return (char) getBytes()[index];
    }

    @Override
    default @NotNull CharSequence subSequence(int start, int end) {
        return new String(getBytes()).subSequence(start, end);
    }

    class Basic implements Credentials {

        // Static initializers

        public static @NotNull Basic parse(@NotNull String basic) throws ParseException {
            int lastIndex = basic.lastIndexOf(":");

            if (lastIndex == -1) {
                throw new ParseException("basic authorization missing ':' separator", 0);
            }

            @NotNull String prefix = basic.substring(0, lastIndex);
            @NotNull String suffix = basic.substring(lastIndex + 1);

            return new Basic(prefix, suffix.toCharArray());
        }

        // Object

        private final @NotNull String username;
        private final char[] password;

        public Basic(@NotNull String username, char[] password) {
            this.username = username;
            this.password = password;
        }

        // Getters

        @Contract(pure = true)
        public final @NotNull String getUsername() {
            return this.username;
        }

        public final char[] getPassword() {
            return this.password;
        }

        // Implementations

        @Override
        public void flush() {
            Arrays.fill(password, (char) 0);
        }

        @Override
        public byte[] getBytes() {
            return toString().getBytes();
        }

        @Override
        public @NotNull String toString() {
            return (getUsername() + ":" + new String(getPassword()));
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof Basic)) return false;
            Basic basic = (Basic) object;
            return Objects.equals(getUsername(), basic.getUsername()) && Arrays.equals(getPassword(), basic.getPassword());
        }
        @Override
        public int hashCode() {
            int result = Objects.hash(getUsername());
            result = 31 * result + Arrays.hashCode(getPassword());
            return result;
        }

    }

}
