package uq.pac.rsvp.policy.datalog;

import java.nio.file.Path;

public class DLTest {
    public static Path pathOf(String resource) {
        return Path.of(DLTest.class.getClassLoader().getResource(resource).getPath());
    }
}
