package uq.pac.rsvp.policy.ast;

import org.junit.jupiter.api.Test;
import uq.pac.rsvp.StdLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class TestUtil {

    public final static StdLogger LOGGER = new StdLogger();

    public final static Path ROOTDIR =
            Path.of(System.getProperty("test.rootdir")).toAbsolutePath();
    public final static Path RESOURCEDIR =
            Path.of(ROOTDIR.toString(), "src", "test", "resources");
    public final static Path TMPDIR =
            Path.of(ROOTDIR.toString(), "build", "tmp");
    public final static Path CEDAR;


    public final static boolean GENERATE_ORACLES = false;

    @Test
    void generateOracles() {
        assertFalse(GENERATE_ORACLES);
    }

    static {
        CEDAR = findExecutable("cedar");
        try {
            Files.createDirectories(TMPDIR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Path findExecutable(String executable) {
        String sysPath = System.getenv("PATH");
        return Stream.of(sysPath.split(Pattern.quote(File.pathSeparator)))
                .map(Path::of)
                .map(path -> {
                    path = path.resolve(executable);
                    return Files.isExecutable(path) ? path : null;
                })
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public static Path getResourceDir(String ...names) {
        return Path.of(RESOURCEDIR.toString(), names);
    }

    public static List<Path> findFiles(Path dir, String ext) {
        try (Stream<Path> paths = Files.list(dir)) {
            List<Path> files = new ArrayList<>();
            paths.forEach(p -> {
                if (Files.isRegularFile(p) && p.toString().endsWith(ext)) {
                    files.add(p);
                } else if (Files.isDirectory(p)) {
                    files.addAll(findFiles(p, ext));
                }
            });
            return files;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
