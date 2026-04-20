package uq.pac.rsvp.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FileSourceTest {

    private final static String FILENAME = "test.txt";
    private final static String SOURCE = """
        
        !
        abc
        de
        
        """;

    private final static FileSource FS;
    static {
        FS = new FileSource(FILENAME, SOURCE);
    }

    void ok(FileSource fs, int of, int ln, int col) {
        LineLoc loc = fs.getLineLoc(of);
        assertEquals(ln + ":" + col, loc.toString());
        assertEquals(of, FS.getPosition(ln, col));
    }

    void offsetFail(FileSource fs, int of) {
        try {
            fs.getLineLoc(of);
            fail("Unexpected pass");
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            String expected = "Invalid position: %d".formatted(of);
            assertEquals(expected, msg);
        }
    }

    void lineLocFail(FileSource fs, int ln, int col, String msg) {
        try {
            fs.getPosition(ln, col);
            fail("Unexpected pass");
        } catch (RuntimeException e) {
            assertEquals(msg, e.getMessage());
        }
    }

    @Test
    void test() {
        offsetFail(FS, -1);
        offsetFail(FS, 0);
        ok(FS, 1, 1, 1);
        ok(FS, 2, 2, 1);
        ok(FS, 3, 2, 2);
        ok(FS, 4, 3, 1);
        ok(FS, 5, 3, 2);
        ok(FS, 6, 3, 3);
        ok(FS, 7, 3, 4);
        ok(FS, 8, 4, 1);
        ok(FS, 9, 4, 2);
        ok(FS, 10, 4, 3);
        ok(FS, 11, 5, 1);
        offsetFail(FS,12);

        FileSource empty = new FileSource("", "");
        offsetFail(empty, 1);

        FileSource one = new FileSource("", "1");
        ok(one, 1, 1, 1);
        offsetFail(one, 2);

        FileSource two = new FileSource("", "\n1");
        ok(two, 1, 1, 1);
        ok(two, 2, 2, 1);
        offsetFail(two, 3);

        // Invalid line/column
        lineLocFail(FS, -1, 1, "Invalid line: -1");
        lineLocFail(FS, 0, 1, "Invalid line: 0");
        lineLocFail(FS, 6, 1, "Invalid line: 6");
        lineLocFail(FS, 7, 1, "Invalid line: 7");
        lineLocFail(FS, 1, -1, "Invalid column: -1");
        lineLocFail(FS, 1, 0, "Invalid column: 0");
        lineLocFail(FS, 1, 2, "Invalid column: 2");
        lineLocFail(FS, 1, 3, "Invalid column: 3");
        lineLocFail(FS, 3, 5, "Invalid column: 5");
    }
}
