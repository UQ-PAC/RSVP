package uq.pac.rsvp.policy.datalog.invariant;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Representation of a quantifier for invariant that captures
 */
public class Quantifier {
    public enum Scope {
        ALL("all"),
        SOME("some"),
        NONE("none");

        Scope(String quantifier) {
            this.value = quantifier;
        }

        private final String value;

        @Override
        public String toString() {
            return value;
        }
    }

    public record Variable(String name, String type) { }

    private final List<Variable> variables;
    private final Scope scope;

    public Quantifier() {
        this(Scope.ALL, Collections.emptyList());
    }

    public Quantifier(Scope scope, List<Variable> variables) {
        this.variables = List.copyOf(variables);
        this.scope = scope;
    }

    public Scope getScope() {
        return scope;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public boolean isEmpty() {
        return variables.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String vars = variables.stream()
                .map(e -> e.name + ": " + e.type)
                .collect(Collectors.joining(", "));
        if (!vars.isEmpty()) {
            sb.append("for ")
                    .append(scope)
                    .append(" ")
                    .append(vars);
        }
        return sb.toString();
    }

}
