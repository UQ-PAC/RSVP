package uq.pac.rsvp.policy.ast;

public class SourceLoc {

    public static final SourceLoc MISSING = new SourceLoc("unknown", -1, 0);

    public final String file;
    public final int offset;
    public final int len;

    public SourceLoc(String file, int offset, int len) {
        this.file = file;
        this.offset = offset;
        this.len = len;
    }
}
