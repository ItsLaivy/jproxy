import codes.laivy.proxy.http.core.Credentials.Basic;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.ParseException;

public final class CredentialsTests {

    @Test
    public void basic() throws ParseException {
        @NotNull String basicString = "username:password";
        @NotNull Basic expected = new Basic("username", "password");

        Assertions.assertEquals(Basic.parse(basicString), expected);
    }

}
