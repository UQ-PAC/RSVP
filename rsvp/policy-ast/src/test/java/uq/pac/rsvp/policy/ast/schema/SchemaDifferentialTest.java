package uq.pac.rsvp.policy.ast.schema;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import uq.pac.rsvp.policy.ast.antlrschema.parser.AntlrSchemaParser;

import java.io.IOException;
import java.lang.module.ResolutionException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SchemaDifferentialTest {

    public final static Path ROOTDIR =
            Path.of(System.getProperty("test.rootdir")).toAbsolutePath();
    public final static Path RESOURCEDIR =
            Path.of(ROOTDIR.toString(), "src", "test", "resources");

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

    // Negative test catching resolution errors
    @TestFactory
    Collection<DynamicTest> test() {
        Path dir = getResourceDir("antlr", "negative");
        return findFiles(dir, ".cedarschema").stream().map(path -> {

            String oracle;
            String prefix = "// EXPECTED:";
            try {
                oracle = Files.readAllLines(path).stream()
                        .map(String::trim)
                        .filter(l -> l.startsWith(prefix))
                        .map(s -> s.substring(prefix.length()).trim())
                        .findFirst()
                        .orElseThrow();
            } catch (IOException e) {
                throw new RuntimeException(e.getCause());
            }

            return DynamicTest.dynamicTest(path.getFileName().toString(), () -> {
                try {
                    AntlrSchemaParser.parse(path);
                } catch (ResolutionException e) {
                    assertEquals(oracle, e.getMessage().trim());
                }
            });
        }).toList();
    }

}
