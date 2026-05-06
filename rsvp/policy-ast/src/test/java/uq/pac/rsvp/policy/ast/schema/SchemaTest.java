package uq.pac.rsvp.policy.ast.schema;

import com.cedarpolicy.model.exception.InternalException;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import uq.pac.rsvp.StdLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.cedarpolicy.model.schema.Schema.JsonOrCedar.Json;
import static org.junit.jupiter.api.Assertions.*;

public class SchemaTest {

    private final static StdLogger LOGGER = new StdLogger();

    public final static Path ROOTDIR =
            Path.of(System.getProperty("test.rootdir")).toAbsolutePath();
    public final static Path RESOURCEDIR =
            Path.of(ROOTDIR.toString(), "src", "test", "resources");
    public final static Path TMPDIR =
            Path.of(ROOTDIR.toString(), "build", "tmp");
    public final static boolean GENERATE_ORACLES = false;
    public final static Path CEDAR;

    static {
        CEDAR = findExecutable("cedar");
        try {
            Files.createDirectories(TMPDIR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void oracles() {
        assertFalse(GENERATE_ORACLES);
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

    record ValidationTest(String name, String text, String expected) { }

    private List<ValidationTest> readTests(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file);
        List<ValidationTest> tests = new ArrayList<>();
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

            ValidationTest test = new ValidationTest(testName, sb.toString(), expected);
            tests.add(test);
        }
        return tests;
    }

    // Validation errors and legal behaviours
    @TestFactory
    Collection<DynamicTest> test() {
        Path dir = getResourceDir("schema", "validation");
        List<DynamicTest> tests = new ArrayList<>();
        findFiles(dir, ".cedarschema").forEach(path -> {
            try {
                readTests(path).forEach(t -> {
                    DynamicTest dt = DynamicTest.dynamicTest(t.name, () -> {
                        try {
                            Schema.parse("test", t.text);
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

    private static Path findExecutable(String executable) {
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

    // Get a normalised representation of a cedar file with resolved types by converting it to
    // JSON with resolved types and then back to cedar
    static String getNormalisedCedarSchemaText(Path schemaPath) throws InterruptedException, IOException, InternalException {


        // Define the command and its arguments
        ProcessBuilder pb = new ProcessBuilder(CEDAR.toString(),
                "translate-schema",
                "--direction=cedar-to-json-with-resolved-types",
                "-s",
                schemaPath.toAbsolutePath().toString());
        Process process = pb.start();

        StringBuilder stdout = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            stdout.append(line).append('\n');
        }

        assertEquals(0, process.waitFor());
        return com.cedarpolicy.model.schema.Schema.parse(Json, stdout.toString()).toCedarFormat();
    }

    // Differential test for overall schema representation.
    // Generate a cedar schema, convert it to string and compare to the output of cedar
    void completenessTest(Path path) {
        try {
            if (CEDAR == null) {
                LOGGER.warning("Cedar executable not found. Skip schema completeness tests for: "
                        + path.getFileName().toString());
            }
            Assumptions.assumeTrue(CEDAR != null);
            Schema schema = Schema.parse(path);
            System.out.println(schema.toString());
            Path schemaPath = Files.createTempFile(TMPDIR, path.getFileName().toString(), null);
            Files.writeString(schemaPath, schema.toString());
            String expected = getNormalisedCedarSchemaText(path);
            String actual = getNormalisedCedarSchemaText(schemaPath);
            assertEquals(expected, actual);
        } catch (IOException | InternalException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @TestFactory
    List<DynamicTest> completeness() {
        Path dir = getResourceDir("schema", "completeness");
        List<DynamicTest> tests = new ArrayList<>();
        findFiles(dir, ".cedarschema").forEach(path -> {
            DynamicTest test = DynamicTest.dynamicTest(path.getFileName().toString(),
                    () -> completenessTest(path));
            tests.add(test);
        });
        return tests;
    }
}
