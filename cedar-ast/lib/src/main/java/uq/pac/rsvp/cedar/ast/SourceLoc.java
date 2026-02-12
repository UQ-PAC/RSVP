package uq.pac.rsvp.cedar.ast;

public class SourceLoc {
    public final String file;
    public final int offset;
    public final int len;

    public SourceLoc(String file, int offset, int len) {
        this.file = file;
        this.offset = offset;
        this.len = len;
    }
}
