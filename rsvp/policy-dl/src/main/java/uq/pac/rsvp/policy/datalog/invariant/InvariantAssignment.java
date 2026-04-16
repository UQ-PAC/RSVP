package uq.pac.rsvp.policy.datalog.invariant;

import uq.pac.rsvp.policy.datalog.translation.Relation;

import java.util.*;

import static uq.pac.rsvp.policy.datalog.util.Assertion.require;

/**
 * Representation of variable assignment for an invariant quantifier
 */
public class InvariantAssignment {
    private final Map<String, String> assignment;

    public InvariantAssignment(Map<String, String> assignment) {
        this.assignment = Map.copyOf(assignment);
    }

    public String getValue(String var) {
        return assignment.get(var);
    }

    public Set<String> getVariables() {
        return assignment.keySet();
    }

    @Override
    public int hashCode() {
        return assignment.hashCode();
    }

    @Override
    public String toString() {
        return assignment.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (o instanceof InvariantAssignment a) {
            return a.assignment.equals(this.assignment);
        }
        return false;
    }

    public static Set<InvariantAssignment> getAssignments(Relation relation) {
        Set<InvariantAssignment> assignments = new HashSet<>();
        List<String> header = relation.getHeaders();
        for (List<String> row : relation.getRows()) {
            Map<String, String> assignment = new HashMap<>();
            require(header.size() == row.size());
            for (int i = 0; i < header.size(); i++) {
                assignment.put(header.get(i), row.get(i));
            }
            assignments.add(new InvariantAssignment(assignment));
        }
        return assignments;
    }
}
