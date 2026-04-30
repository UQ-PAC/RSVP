package uq.pac.rsvp.support;

/**
 * Line location
 */
public record LineLoc(int line, int column) {
    @Override
    public String toString() {
        return line + ":" + column;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        } else if (other == this) {
            return true;
        } else if (other instanceof LineLoc(int l, int c)) {
            return l == this.line && c == this.column;
        }
        return false;
    }
}
