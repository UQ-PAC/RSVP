package uq.pac.rsvp.support;

/**
 * Describes a location (range) within a textual source, given by offset (0-based) and length,
 * as well as line and column (1-based).
 */
public class SourceLoc {

    public static final SourceLoc MISSING =
            new SourceLoc(null, -1, 0, null, null);

    public final String file;
    public final int offset;
    public final int len;
    private final LineLoc start;
    private final LineLoc end;

    SourceLoc(String file, int offset, int len, LineLoc start, LineLoc end) {
        this.file = file;
        this.offset = offset;
        this.len = len;
        this.start = start;
        this.end = end;
    }

    public String getFile() {
        return file;
    }

    public LineLoc getStartLoc() {
        return start;
    }

    public LineLoc getEndLoc() {
        return end;
    }

    @Override
    public String toString() {
        String loc = "%s:%d:%d".formatted(file, offset, len);
        if (start != null && end != null) {
            loc += " [%s-%s]".formatted(start.toString(), end.toString());
        }
        return loc;
    }
}
