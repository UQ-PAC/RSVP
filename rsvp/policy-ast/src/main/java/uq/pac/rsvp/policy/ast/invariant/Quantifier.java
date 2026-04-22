package uq.pac.rsvp.policy.ast.invariant;

import uq.pac.rsvp.policy.ast.PolicyAstNode;
import uq.pac.rsvp.policy.ast.expr.TypeExpression;
import uq.pac.rsvp.policy.ast.expr.VariableExpression;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;
import uq.pac.rsvp.support.SourceLoc;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Representation of a quantifier for invariant that captures
 */
public class Quantifier extends PolicyAstNode {

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

    public record Variable(VariableExpression name, TypeExpression type) { }

    private final List<Variable> variables;
    private final Scope scope;

    public Quantifier(Scope scope, List<Variable> variables, SourceLoc location) {
        super(location);
        this.variables = List.copyOf(variables);
        this.scope = scope;
    }

    public Quantifier(Scope scope, List<Variable> variables) {
        this(scope, variables, SourceLoc.MISSING);
    }

    public Quantifier() {
        this(Scope.ALL, Collections.emptyList());
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
    public void accept(PolicyVisitor visitor) {
        visitor.visitQuantifier(this);
    }

    @Override
    public <T> T compute(PolicyComputationVisitor<T> visitor) {
        return visitor.visitQuantifier(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String vars = variables.stream()
                .map(e -> e.name.getReference() + ": " + e.type.getValue())
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
