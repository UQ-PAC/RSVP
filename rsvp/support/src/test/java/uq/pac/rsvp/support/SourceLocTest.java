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

    void locOk(int of, int ln, int ls, int cs, int le, int ce) {
        SourceLoc loc = FS.getSourceLoc(of, ln);
        String expected = "%s:%d:%d [%d:%d-%d:%d]".formatted(FILENAME, of, ln, ls, cs, le, ce);
        assertEquals(expected, loc.toString());
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
        locOk(0, 1, 1,1, 1, 1); // T (The)
        locOk(0, 3, 1,1, 1, 3); // The
        locOk(4, 5, 1,5, 1, 9); // quick
        locOk(17, 3, 2,2, 2, 4); // fox
        locOk(25, 1, 2,10, 2, 10); // s (jumps)
        locOk(29, 4, 3,3, 3, 6); // over
        locOk(46, 3, 4,9, 4, 11); // dog

        locFail(47, 4);
        locFail(-1, 4);
        locFail(4, -2);
        locFail(4, 0);
    }
}
