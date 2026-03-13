package uq.pac.rsvp.policy.datalog.ast;

/**
 * Datalog variable:
 * <code>
 *   Variable ::= IDENT
 * </code>
 */
public final class DLVar extends DLTerm {
    private final String name;

    public enum Functor {
        TO_NUMBER("to_number"),
        TO_STRING("to_string"),
        TO_FLOAT("to_float"),
        ORD("ord");

        private final String functor;

        Functor(String fn) {
            this.functor = fn;
        }
    }

    public DLVar(String name) {
        this.name = name;
    }

    @Override
    protected String stringify() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DLVar s) {
            return s.name.equals(name);
        }
        return false;
    }

    public DLVar functor(Functor functor) {
        return new DLVar("%s(%s)".formatted(functor.functor, name));
    }

    public String getName() {
        return name;
    }
}
