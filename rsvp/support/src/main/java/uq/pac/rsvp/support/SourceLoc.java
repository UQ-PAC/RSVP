package uq.pac.rsvp.support;

public class SourceLoc {

    public static final SourceLoc MISSING = new SourceLoc();

    public final String file;
    public final int offset;
    public final int len;

    public SourceLoc(String file, int offset, int len) {
        this.file = file;
        this.offset = offset;
        this.len = len;
    }

    public SourceLoc() {
        this.file = "unknown";
        this.offset = -1;
        this.len = 0;
    }
}
