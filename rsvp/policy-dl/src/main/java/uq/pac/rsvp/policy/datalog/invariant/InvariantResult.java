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
            case ALL ->
                // Invariants using `ALL` quantifiers scope are rewritten to NONE
                throw new TranslationError("Unexpected invariant scope: " + scope);
            case SOME -> this.holds = !relation.isEmpty();
            case NONE -> this.holds = relation.isEmpty();
            default -> throw new TranslationError("Unreachable");
        }
    }

    public Invariant getInvariant() {
        return invariant;
    }

    public Set<Assignment> getAssignments() {
        if (!holds) {
            throw new TranslationError("Assignments cannot be extracted for a failing invariant");
        }

        switch (invariant.getQuantifier().getScope()) {
            case NONE -> {
                if (!assignments.isEmpty()) {
                    throw new TranslationError("Expected no counterexamples but found some");

                }
            }
            case SOME -> {
                if (assignments.isEmpty()) {
                    throw new TranslationError("Expected counterexamples but found none");
                }
            }
            default -> throw new TranslationError("Unreachable");
        }

        return assignments;
    }

    public boolean holds() {
        return holds;
    }

    public Set<Assignment> getCounterExamples() {
        if (holds) {
            throw new TranslationError("Counterexample cannot be extracted for a passing invariant");
        }

        switch (invariant.getQuantifier().getScope()) {
            case NONE -> {
                if (assignments.isEmpty()) {
                    throw new TranslationError("Expected counterexamples but found none");
                }
            }
            case SOME -> {
                if (!assignments.isEmpty()) {
                    throw new TranslationError("Expected no counterexamples but found some");
                }
            }
            default -> throw new TranslationError("Unreachable");
        }
        return assignments;
    }
}
