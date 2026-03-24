package uq.pac.rsvp.policy.datalog.invariant;

import uq.pac.rsvp.policy.ast.expr.Expression;
import uq.pac.rsvp.policy.ast.expr.TypeExpression;

import java.util.Map;
import java.util.stream.Collectors;

public class Invariant {

    private final Quantifier quantifier;
    private final Expression expression;
    private final Map<String, TypeExpression> types;

    public Invariant(Quantifier quantifier, Expression expression, Map<String, TypeExpression> types) {
        this.quantifier = quantifier;
        this.expression = expression;
        this.types = Map.copyOf(types);
    }

    public Quantifier getQuantifier() {
        return quantifier;
    }

    public Expression getExpression() {
        return expression;
    }

    public Map<String, TypeExpression> getTypes() {
        return types;
    }

    public TypeExpression getType(String var) {
        return types.get(var);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(expression);
        String vars = types.keySet().stream()
                .sorted()
                .map(e -> e + ": " + types.get(e))
                .collect(Collectors.joining(", "));
        if (!vars.isEmpty()) {
            sb.append(" for ")
                    .append(quantifier)
                    .append(" ")
                    .append(vars);
        }
        return sb.toString();
    }
}
