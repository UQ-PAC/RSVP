package uq.pac.rsvp.policy.datalog.ast;

/**
 * Souffle functor application:
 * <code>
 *   Variable ::= IDENT '(' IDENT ')'
 * </code>
 */
public final class DLFunctor extends DLTerm {
    private final Functor functor;
    private final DLTerm term;

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

    public DLFunctor(Functor functor, DLTerm term) {
        this.functor = functor;
        this.term = term;
    }

    @Override
    protected String stringify() {
        return "%s(%s)".formatted(functor.functor, term.stringify());
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DLFunctor s) {
            return s.functor.equals(functor) && s.term.equals(term);
        }
        return false;
    }
}
