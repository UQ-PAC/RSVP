package uq.pac.rsvp.support;

/**
 * Line location
 */
public record LineLoc(int line, int column) {
    @Override
    public String toString() {
        return line + ":" + column;
    }
}
