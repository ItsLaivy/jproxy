import codes.laivy.proxy.http.core.ContentType;
import codes.laivy.proxy.http.core.NameValuePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Arrays;

public final class ContentTypeTests {

    private static final class Meta {

        private final @NotNull String input;

        private final @NotNull String mineType;
        private final @Nullable Charset charset;
        private final @NotNull NameValuePair[] parameters;


        public Meta(@NotNull String input, @NotNull String mineType, @Nullable Charset charset, @NotNull NameValuePair[] parameters) {
            this.input = input;
            this.mineType = mineType;
            this.charset = charset;
            this.parameters = parameters;
        }
    }

    @Test
    public void parse() throws ParseException {
        @NotNull Meta[] metas = new Meta[] {
                new Meta("multipart/form-data; charset=ISO-8859-1; boundary=something", "multipart/form-data", StandardCharsets.ISO_8859_1, new NameValuePair[] { NameValuePair.create("boundary", "something") }),
                new Meta("multipart/form-data;charset=ISO-8859-1;boundary=something", "multipart/form-data", StandardCharsets.ISO_8859_1, new NameValuePair[] { NameValuePair.create("boundary", "something") }),
                new Meta("application/json", "application/json", StandardCharsets.UTF_8, new NameValuePair[0]),
                new Meta("application/json;charset=UTF-16", "application/json", StandardCharsets.UTF_16, new NameValuePair[0]),
                new Meta("application/json;charset=UTF_16", "application/json", StandardCharsets.UTF_16, new NameValuePair[0]),
                new Meta("application/json;charset=utf_16", "application/json", StandardCharsets.UTF_16, new NameValuePair[0])
        };

        for (@NotNull Meta meta : metas) {
            @NotNull ContentType contentType = ContentType.parse(meta.input);

            Assertions.assertEquals(contentType.getMimeType(), meta.mineType, meta.input);
            Assertions.assertEquals(contentType.getCharset(), meta.charset, meta.input);
            Assertions.assertTrue(Arrays.equals(contentType.getParameters(), meta.parameters), meta.input);
        }
    }

}
