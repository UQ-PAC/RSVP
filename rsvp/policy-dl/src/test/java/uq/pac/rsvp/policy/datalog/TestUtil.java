package uq.pac.rsvp.policy.datalog;

import uq.pac.rsvp.policy.datalog.translation.TranslationError;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

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
    public static Path pathOf(String resource) {
        return Path.of(TestUtil.class.getClassLoader().getResource(resource).getPath());
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

}
