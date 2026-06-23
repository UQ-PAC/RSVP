/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SourceLocTest {

    private final static String FILENAME = "test.txt";
    private final static String SOURCE = """
        The quick brown
         fox jumps
          over the
           lazy dog""";

    private final static FileSource FS = new FileSource(FILENAME, SOURCE);

    void locOk(SourceLoc loc, int of, int ln, int ls, int cs, int le, int ce) {
        String expected = "%s:%d:%d [%d:%d-%d:%d]".formatted(FILENAME, of, ln, ls, cs, le, ce);
        assertEquals(expected, loc.toString());
    }

    SourceLoc locOk(int of, int ln, int ls, int cs, int le, int ce) {
        SourceLoc loc = FS.getSourceLoc(of, ln);
        locOk(loc, of, ln, ls, cs, le, ce);
        return loc;
    }

    void locFail(int offset, int length) {
        try {
            FS.getSourceLoc(offset, length);
            fail("Unexpected pass");
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            String expected =
                    "Invalid source location: %s:%d:%d".formatted(FILENAME, offset, length);
            assertEquals(expected, msg);
        }
    }

    @Test
    void locationTest() {
        SourceLoc all = locOk(0, 49, 1,1, 4, 11); // Entire file
        SourceLoc t = locOk(0, 1, 1,1, 1, 1); // T (The)
        SourceLoc the = locOk(0, 3, 1,1, 1, 3); // The
        SourceLoc quick = locOk(4, 5, 1,5, 1, 9); // quick
        SourceLoc fox = locOk(17, 3, 2,2, 2, 4); // fox
        SourceLoc s = locOk(25, 1, 2,10, 2, 10); // s (jumps)
        SourceLoc over = locOk(29, 4, 3,3, 3, 6); // over
        SourceLoc dog = locOk(46, 3, 4,9, 4, 11); // dog

        locFail(47, 4);
        locFail(-1, 4);
        locFail(4, -2);
        locFail(4, 0);

        locOk(FS.getSourceLoc(the, the), 0, 3, 1,1, 1, 3);
        locOk(FS.getSourceLoc(fox, fox), 17, 3, 2,2, 2, 4);
        locOk(FS.getSourceLoc(dog, dog), 46, 3, 4,9, 4, 11);
        locOk(FS.getSourceLoc(t, the), 0, 3, 1,1, 1, 3);
        locOk(FS.getSourceLoc(t, dog), 0, 49, 1,1, 4, 11);
        locOk(FS.getSourceLoc(fox, dog), 17, 32, 2,2, 4, 11);
        locOk(FS.getSourceLoc(s, over), 25, 8, 2,10, 3, 6);

        // Invalid
        assertThrows(RuntimeException.class, () -> FS.getSourceLoc(the, t));
        assertThrows(RuntimeException.class, () -> FS.getSourceLoc(over, fox));
        assertThrows(RuntimeException.class, () -> FS.getSourceLoc(s, all));

        assertFalse(FS.isValid(SourceLoc.MISSING));
    }
}
