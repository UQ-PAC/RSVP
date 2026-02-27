package uq.pac.rsvp.policy.datalog.ast;

/**
 * Declaration (typed) term
 * <code>
 *   DeclTerm := IDENT ':' Type
 * </code>
 */
public class DLDeclTerm extends DLNode {
    private final DLType type;
    private final String name;

    public DLDeclTerm(String name, DLType type) {
        this.name = name;
        this.type = type;
    }

    @Override
    protected String stringify() {
        return name + ": " + type;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DLDeclTerm t) {
            return t.name.equals(name) && t.type.equals(type);
        }
        return false;
    }

    public static DLDeclTerm symbolic(String name) {
        return new DLDeclTerm(name, DLType.SYMBOL);
    }

    public static DLDeclTerm numeric(String name) {
        return new DLDeclTerm(name, DLType.NUMBER);
    }
}
