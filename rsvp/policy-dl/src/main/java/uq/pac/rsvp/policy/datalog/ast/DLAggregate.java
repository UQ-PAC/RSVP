package uq.pac.rsvp.policy.datalog.ast;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Souffle aggregate function:
 * <code>
 *   Aggregate ::= IDENT : { Atom [ ',' Atom]* }
 * </code>
 */
public final class DLAggregate extends DLTerm {
    private final Aggregate aggregate;
    private final List<DLAtom> body;

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

    public DLAggregate(Aggregate aggregate, List<DLAtom> atoms) {
        this.aggregate = aggregate;
        this.body = List.copyOf(atoms);
    }

    public DLAggregate(Aggregate aggregate, DLAtom ...atoms) {
        this.aggregate = aggregate;
        this.body = Arrays.stream(atoms).toList();
    }

    @Override
    protected String stringify() {
        String atoms = body.stream().map(DLAtom::stringify).collect(Collectors.joining(", "));
        return "%s : { %s }".formatted(aggregate.aggregate, atoms);
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

    public List<DLAtom> getBody() {
        return body;
    }
}
