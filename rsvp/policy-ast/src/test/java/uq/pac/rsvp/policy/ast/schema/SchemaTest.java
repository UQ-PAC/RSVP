/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.ast.schema;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import uq.pac.rsvp.policy.ast.TestUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static uq.pac.rsvp.policy.ast.TestUtil.*;

public class SchemaTest {

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
        Path dir = TestUtil.getResourceDir("schema", "validation");
        List<DynamicTest> tests = new ArrayList<>();
        TestUtil.findFiles(dir, ".cedarschema").forEach(path -> {
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

    // Differential test for overall schema representation.
    // Generate a cedar schema, convert it to string and compare to the output of cedar
    void completenessTest(Path path) {
        try {
            Schema schema = Schema.parse(path);
            Path expected = Path.of(path.getParent().toString(), path.getFileName() + ".expected");
            if (GENERATE_ORACLES) {
                Files.writeString(expected, schema.toString());
            }
            assertEquals(Files.readString(expected), schema.toString());
        } catch (IOException e) {
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
