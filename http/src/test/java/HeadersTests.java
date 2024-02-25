import codes.laivy.proxy.http.core.headers.HeaderKey;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public final class HeadersTests {

    @Test
    public void accept() {
        @NotNull HeaderKey key = HeaderKey.ACCEPT;
        assert key.getPattern() != null;

        @NotNull String[] texts = new String[] {
                "application/json;q=0.9, video/mpv",
                "text/html; q=0.9, text/plain; q=0.8",
                "text/css; q=0.8, application/javascript",
                "image/gif; q=0.7, image/webp; q=0.6",
                "audio/wav; q=0.5, audio/webm; q=0.4",
                "video/ogg; q=0.3, video/webm; q=0.2",
                "application/xhtml+xml; q=0.1, application/rss+xml; q=0.05",
                "APPLICATION/xhtml+xml; q=0.1, application/rss+xml; q=0.05", // Uppercase
                "text/html, application/xhtml+xml, application/xml;q=0.9, */*;q=0.8",
                "*/*",
                "image/*",
                "text/html",
        };

        for (@NotNull String text : texts) {
            if (!text.matches(key.getPattern().pattern())) {
                throw new IllegalStateException(key.getName() + ": regex not matches the '" + text + "'");
            }
        }
    }

}
