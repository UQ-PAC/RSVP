package uq.pac.rsvp.policy.ast.expr;

import static uq.pac.rsvp.policy.ast.expr.Expression.ExprType.Set;

import java.util.HashSet;
import java.util.Set;

import uq.pac.rsvp.policy.ast.SourceLoc;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;

public class SetExpression extends Expression {

    private final Set<Expression> elements;

    public SetExpression(Set<Expression> elements, SourceLoc source) {
        super(Set, source);
        this.elements = new HashSet<>(elements);
    }

    public SetExpression(Set<Expression> elements) {
        this(elements, SourceLoc.MISSING);
    }

    public Set<Expression> getElements() {
        return elements;
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitSetExpr(this);
    }

    @Override
    public <T> T compute(PolicyComputationVisitor<T> visitor) {
        return visitor.visitSetExpr(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');

        String sep = "";
        for (Expression elem : elements) {
            sb.append(sep);
            sb.append(elem.toString());
            sep = ", ";
        }

        sb.append(']');
        return sb.toString();
    }

}
