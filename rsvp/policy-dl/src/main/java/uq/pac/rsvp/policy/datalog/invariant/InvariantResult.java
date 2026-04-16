package uq.pac.rsvp.policy.datalog.invariant;

import java.util.Set;

import uq.pac.rsvp.policy.datalog.translation.Relation;
import uq.pac.rsvp.policy.datalog.translation.TranslationError;

/**
 * Result of evaluating an invariant
 */
public class InvariantResult {

    private final boolean holds;
    private final Invariant invariant;
    private final Set<InvariantAssignment> assignments;

    public InvariantResult(Invariant invariant, Relation relation) {
        this.invariant = invariant;
        this.assignments = Assignment.getAssignments(relation);
        Quantifier.Scope scope = invariant.getQuantifier().getScope();
        switch (scope) {
            case SOME -> this.holds = !relation.isEmpty();
            case NONE, ALL -> this.holds = relation.isEmpty();
            default -> throw new TranslationError("Unreachable");
        }
    }

    public Invariant getInvariant() {
        return invariant;
    }

    /**
     * Get variable assignments which satisfy the invariant (if it holds) or provide a
     * counterexample (if it does not hold).
     * <p>
     * Note that for a `for all`-quantified variable we generate no assignments if the
     * invariant holds (all possible values are satisfactory). Similarly, for a `for some`-
     * quantified variable in an invariant that does <i>not</i> hold, there is no specific
     * counterexample.
     * <p>
     * (If assignments are actually needed for a `for all`-quantified variable, it is possible
     * to transform the `for all` into a `for some` which will generate assignments; the 'holds'
     * condition is then weaker, so whether the original invariant holds needs to be tested
     * separately).
     */
    public Set<InvariantAssignment> getAssignments() {
        return assignments;
    }

    public boolean holds() {
        return holds;
    }
}
