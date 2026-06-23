/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.datalog.translation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static uq.pac.rsvp.Assertion.require;

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

    public String toHumanReadableString() {
        StringBuilder result = new StringBuilder("When ");
        List<String> variables = new ArrayList<>(assignment.keySet());

        for (int i = 0; i < assignment.size(); i++) {
            String var = variables.get(i);
            String val = assignment.get(var);

            if (i > 0 && i == assignment.size() - 1) {
                result.append(" and ");
            } else if (i > 0) {
                result.append(", ");
            }

            result.append(var);
            result.append(" is ");
            result.append(val);
        }

        return result.toString();
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
