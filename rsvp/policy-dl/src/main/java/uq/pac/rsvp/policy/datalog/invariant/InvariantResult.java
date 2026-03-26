package uq.pac.rsvp.policy.datalog.invariant;

import uq.pac.rsvp.policy.datalog.translation.Relation;
import uq.pac.rsvp.policy.datalog.translation.TranslationError;

import java.util.Set;

/**
 * Result of evaluating an invariant
 */
public class InvariantResult {

    private final boolean holds;
    private final Invariant invariant;
    private final Set<Assignment> assignments;

    public InvariantResult(Invariant invariant, Relation relation) {
        this.invariant = invariant;
        this.assignments = Assignment.getAssignments(relation);
        Quantifier.Scope scope = invariant.getQuantifier().getScope();
        switch (invariant.getQuantifier().getScope()) {
            case SOME -> this.holds = !relation.isEmpty();
            case NONE, ALL -> this.holds = relation.isEmpty();
            default -> throw new TranslationError("Unreachable");
        }
    }

    public Invariant getInvariant() {
        return invariant;
    }

    /**
     * Get assignments. Note that for the `for all` invariant we generate no assignments because
     * the `for all` variant is rewritten to `for none`. If the assignments for the `for all`
     * invariant are needed they can be generated with the `for some` quantifier scope instead
     */
    public Set<Assignment> getAssignments() {
        return assignments;
    }

    public boolean holds() {
        return holds;
    }
}
