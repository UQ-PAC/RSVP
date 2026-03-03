package uq.pac.rsvp.policy.datalog.ast;

/**
 * String literal term (double-quoted)
 */
public final class DLString extends DLTerm {
    private final String value;

    public DLString(String value) {
        this.value = value;
    }

    @Override
    protected String stringify() {
        return "\"" +  value + "\"";
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DLString s) {
            return s.value.equals(value);
        }
        return false;
    }

    public String getValue() {
        return value;
    }
}
