/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.ast;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class TestUtil {
    public final static Path ROOTDIR =
            Path.of(System.getProperty("test.rootdir")).toAbsolutePath();
    public final static Path RESOURCEDIR =
            Path.of(ROOTDIR.toString(), "src", "test", "resources");

    public final static boolean GENERATE_ORACLES = false;

    @Test
    void generateOracles() {
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
}
