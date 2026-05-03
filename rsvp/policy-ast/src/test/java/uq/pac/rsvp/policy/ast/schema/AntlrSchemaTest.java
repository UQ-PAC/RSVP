package uq.pac.rsvp.policy.ast.schema;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import uq.pac.rsvp.policy.ast.antlrschema.AntlrSchema;
import uq.pac.rsvp.policy.ast.antlrschema.parser.AntlrSchemaParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


public class AntlrSchemaTest {

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

    record Test (String name, String text, String expected) { }

    private List<Test> readTests(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file);
        List<Test> tests = new ArrayList<>();
        String name = file.getFileName().toString();
        int index = 1;

        String prefix = "// EXPECTED:";

        while (!lines.isEmpty() && !lines.getFirst().trim().startsWith("// EXPECTED:")) {
            lines.removeFirst();
        }

        while (!lines.isEmpty()) {
            String expected = lines.removeFirst().trim().substring(prefix.length());
            StringBuilder sb = new StringBuilder();
            String testName = name + "-" + index++;

            while (!lines.isEmpty() && !lines.getFirst().startsWith(prefix)) {
                sb.append(lines.removeFirst()).append('\n');
            }

            if (expected.trim().equalsIgnoreCase("success")) {
                expected = null;
            }

            Test test = new Test(testName, sb.toString(), expected);
            tests.add(test);
        }
        return tests;
    }

    // Negative test catching resolution errors
    @TestFactory
    Collection<DynamicTest> test() {
        Path dir = getResourceDir("antlr", "negative");
        List<DynamicTest> tests = new ArrayList<>();
        findFiles(dir, ".cedarschema").forEach(path -> {
            try {
                readTests(path).forEach(t -> {
                    DynamicTest dt = DynamicTest.dynamicTest(t.name, () -> {
                        try {
                            AntlrSchema schema = AntlrSchemaParser.parse("test", t.text);
                            if (t.expected != null) {
                                fail("Expected exception: " + t.expected);
                            }
                        } catch (SchemaResolutionException e) {
                            if (t.expected == null) {
                                throw e;
                            } else {
                                assertEquals(t.expected.trim(), e.getMessage().trim());
                            }
                        }
                    });
                    tests.add(dt);
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return tests;
    }

}
