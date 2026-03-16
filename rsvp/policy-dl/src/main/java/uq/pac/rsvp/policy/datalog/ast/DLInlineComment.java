package uq.pac.rsvp.policy.datalog.ast;

/**
 * Comment node for documentation
*/
public class DLInlineComment extends DLRuleExpr {
    private final String text;

    public DLInlineComment(String text) {
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
        if (obj instanceof DLInlineComment c) {
            return c.text.equals(text);
        }
        return false;
    }
}
