package codes.laivy.proxy.http.core;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContentType {

    // Static initializers

    @ApiStatus.Internal
    private static final @NotNull Pattern PARSE_PATTERN = Pattern.compile("([^;\\s]+)(?:;\\s*charset=([^;\\s]+))?(?:;\\s*(.*))?");
    @ApiStatus.Internal
    private static final @NotNull Pattern PARSE_VALUE_SEPARATOR_PATTERN = Pattern.compile("([^=]+)=([^;\\s]+)");

    public static @NotNull ContentType parse(@NotNull String input) throws ParseException {
        @NotNull String mimeType;
        @Nullable Charset charset = null;
        @NotNull List<NameValuePair> parameterList = new LinkedList<>();

        @NotNull Matcher matcher = PARSE_PATTERN.matcher(input);

        if (matcher.find()) {
            mimeType = matcher.group(1);

            try {
                @Nullable String temp = matcher.group(2);
                if (temp != null) charset = Charset.forName(temp);
            } catch (@NotNull Throwable throwable) {
                throw new ParseException(throwable.getMessage(), matcher.start(2));
            }

            try {
                // Parse parameters
                String params = matcher.group(3);
                if (params != null) {
                    @NotNull Matcher paramMatcher = PARSE_VALUE_SEPARATOR_PATTERN.matcher(params);

                    while (paramMatcher.find()) {
                        parameterList.add(NameValuePair.create(paramMatcher.group(1), paramMatcher.group(2)));
                    }
                }
            } catch (@NotNull Throwable throwable) {
                throw new ParseException(throwable.getMessage(), matcher.start(3));
            }
        } else {
            throw new ParseException("cannot parse '" + input + "' as a valid content type value", 0);
        }

        return new ContentType(mimeType, charset, parameterList.toArray(new NameValuePair[0]));
    }

    public static @NotNull ContentType create(@NotNull String mimeType, @Nullable Charset charset) {
        return new ContentType(mimeType, charset, new NameValuePair[0]);
    }
    public static @NotNull ContentType create(@NotNull String mimeType, @Nullable Charset charset, @NotNull NameValuePair[] parameters) {
        return new ContentType(mimeType, charset, parameters);
    }

    // Provided

    protected static @NotNull ContentType[] initialized = new ContentType[0];

    public static @NotNull ContentType APPLICATION_JSON = create("application/json", StandardCharsets.UTF_8);

    static {
        // Initialize all provided content types
        for (@NotNull Field field : Arrays.stream(ContentType.class.getFields()).filter(field -> field.getType().equals(ContentType.class) && Modifier.isStatic(field.getModifiers())).toArray(Field[]::new)) {
            try {
                @NotNull ContentType type = (ContentType) field.get(null);

                initialized = Arrays.copyOfRange(initialized, 0, initialized.length + 1);
                initialized[initialized.length - 1] = type;
            } catch (@NotNull Throwable throwable) {
                throw new RuntimeException("cannot initialize content type '" + field.getName() + "'", throwable);
            }
        }
    }

    // Object

    private final @NotNull String mimeType;
    private final @Nullable Charset charset;
    private final @NotNull NameValuePair[] parameters;

    private ContentType(@NotNull String mimeType, @Nullable Charset charset, @NotNull NameValuePair @NotNull [] parameters) {
        this.mimeType = mimeType;
        this.parameters = Arrays.copyOf(parameters, parameters.length);

        if (mimeType.toLowerCase().startsWith("multipart/") && parameters.length == 0) {
            throw new NullPointerException("the parameters cannot be empty for multipart media types!");
        }

        @Nullable ContentType implementation = Arrays.stream(initialized).filter(type -> type.mimeType.equalsIgnoreCase(mimeType)).findFirst().orElse(null);
        if (implementation != null) {
            this.charset = charset != null ? charset : implementation.getCharset();
        } else {
            this.charset = charset;
        }
    }

    // Getters

    @Contract(pure = true)
    public final @NotNull String getMimeType() {
        return mimeType;
    }
    @Contract(pure = true)
    public final @Nullable Charset getCharset() {
        return charset;
    }
    @Contract(pure = true)
    public final @NotNull NameValuePair[] getParameters() {
        return parameters;
    }

    // Implementations

    @Override
    public @NotNull String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getMimeType());
        if (getCharset() != null) {
            builder.append("; charset=").append(getCharset());
        }
        for (NameValuePair entry : getParameters()) {
            builder.append("; ").append(entry.getName()).append("=").append(entry.getValue());
        }
        return builder.toString();
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (this == object) return true;
        if (!(object instanceof ContentType)) return false;
        ContentType that = (ContentType) object;
        return Objects.equals(getMimeType(), that.getMimeType()) && Objects.equals(getCharset(), that.getCharset()) && Arrays.equals(getParameters(), that.getParameters());
    }
    @Override
    public int hashCode() {
        int result = Objects.hash(getMimeType(), getCharset());
        result = 31 * result + Arrays.hashCode(getParameters());
        return result;
    }

}
