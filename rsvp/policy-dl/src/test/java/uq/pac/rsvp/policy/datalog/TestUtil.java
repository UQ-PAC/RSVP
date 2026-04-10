package uq.pac.rsvp.policy.datalog;

import org.junit.jupiter.api.Test;
import uq.pac.rsvp.policy.datalog.translation.TranslationError;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uq.pac.rsvp.policy.datalog.util.Assertion.require;

public class TestUtil {
    /**
     * Project build directory
     */
    public final static Path BUILDDIR = Path.of(getProperty("test.builddir")).toAbsolutePath();

    /**
     * Project build destination for temporary datalog specifications
     */
    public final static Path DLTESTDIR = Path.of(BUILDDIR.toString(), "datalog");

    /**
     * Project root
     */
    public final static Path ROOTDIR = Path.of(getProperty("test.rootdir")).toAbsolutePath();

    /**
     * Test resource dir (source tree)
     */
    public final static Path RESOURCEDIR = Path.of(ROOTDIR.toString(), "src", "test", "resources");

    /**
     * Whether to generate ot check test oracles
     */
    public final static boolean GENERATE_ORACLES;
    static {
        String testOracles = System.getProperty("test.oracles.generate");
        GENERATE_ORACLES = testOracles != null && !testOracles.isEmpty();
    }

    /**
     * Get a system property asserting that it is defined
     */
    private static String getProperty(String property) {
        String prop = System.getProperty(property);
        require(prop != null && !prop.isEmpty());
        return prop;
    }

    /**
     * Path of a system resource
     */
    public static Path resourcePath(String resource) {
        return Path.of(TestUtil.class.getClassLoader().getResource(resource).getPath());
    }

    public static Path getDatalogDir(String ...names) {
        return Path.of(DLTESTDIR.toString(), names);
    }

    public static Path getResourceDir(String ...names) {
        return Path.of(RESOURCEDIR.toString(), names);
    }

    /**
     * Remove a directory with contents assuming the contents of a directory
     * are regular files
     */
    public static void removeDirWithContents(Path dir) throws IOException {
        if (dir == null || !Files.exists(dir)) {
            return;
        }

        if (!Files.isDirectory(dir)) {
            throw new TranslationError("Not a directory: " + dir);
        }

        try (Stream<Path> files = Files.list(dir)) {
            for (Path path : files.toList()) {
                if (Files.isRegularFile(path)) {
                    Files.delete(path);
                } else {
                    throw new TranslationError("File " + path + " is not a regular file");
                }
            }
        }
        Files.delete(dir);
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

    public static Path findFile(Path dir, String ext) {
        List<Path> files = findFiles(dir, ext);
        assertEquals(1, files.size(),
                "Expected exactly one file ending with %s in directory %s".formatted(ext, dir.toString()));
        return files.getFirst();
    }

    @Test
    void generateOracles() {
        assertFalse(GENERATE_ORACLES);
    }
}
