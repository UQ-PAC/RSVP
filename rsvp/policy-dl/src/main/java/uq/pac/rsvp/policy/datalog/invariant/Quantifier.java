package uq.pac.rsvp.policy.datalog.invariant;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private final Map<String, String> variables;
    private final Scope scope;

    public Quantifier() {
        this(Scope.ALL, Collections.emptyMap());
    }

    public Quantifier(Scope scope, Map<String, String> variables) {
        this.variables = Map.copyOf(variables);
        this.scope = scope;
    }

    public Scope getScope() {
        return scope;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public Stream<String> variables() {
        return variables.keySet().stream();
    }

    public String getType(String var) {
        return variables.get(var);
    }

    public boolean isEmpty() {
        return variables.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String vars = variables.keySet().stream()
                .sorted()
                .map(e -> e + ": " + variables.get(e))
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
