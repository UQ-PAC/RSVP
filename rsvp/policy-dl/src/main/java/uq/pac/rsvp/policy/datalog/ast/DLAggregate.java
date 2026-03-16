package uq.pac.rsvp.policy.datalog.ast;

/**
 * Souffle aggregate function:
 * <code>
 *   Aggregate ::= IDENT : { Atom }
 * </code>
 */
public final class DLAggregate extends DLTerm {
    private final Aggregate aggregate;
    private final DLAtom body;

    public enum Aggregate {
        COUNT("count"),
        MAX("max"),
        MIN("min"),
        SUM("sum");

        private final String aggregate;

        Aggregate(String aggregate) {
            this.aggregate = aggregate;
        }
    }

    public DLAggregate(Aggregate aggregate, DLAtom body) {
        this.aggregate = aggregate;
        this.body = body;
    }

    @Override
    protected String stringify() {
        return "%s : { %s }".formatted(aggregate.aggregate, body.stringify());
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DLAggregate s) {
            return s.aggregate.equals(aggregate) && s.body.equals(body);
        }
        return false;
    }

    public Aggregate getAggregate() {
        return aggregate;
    }

    public DLAtom getBody() {
        return body;
    }
}
