package uq.pac.rsvp.policy.datalog.ast;

/**
 * Comment node for documentation
*/
public class DLComment extends DLStatement {
    private final String text;

    public DLComment(String text) {
        this.text = text.trim();
    }

    @Override
    protected String stringify() {
        if (text.isEmpty()) {
            return "";
        }
        return text.indexOf('\n') == -1 ? "// " + text : "/* " + text + " */";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DLComment c) {
            return c.text.equals(text);
        }
        return false;
    }
}
