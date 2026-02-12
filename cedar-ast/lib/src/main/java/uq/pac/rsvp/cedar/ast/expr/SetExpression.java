package uq.pac.rsvp.cedar.ast.expr;

import static uq.pac.rsvp.cedar.ast.expr.Expression.ExprType.Set;

import java.util.HashSet;
import java.util.Set;

import uq.pac.rsvp.cedar.ast.SourceLoc;
import uq.pac.rsvp.cedar.ast.visitor.PolicyVisitor;

public class SetExpression extends Expression {

    private Set<Expression> elements;

    public SetExpression(Set<Expression> elements, SourceLoc source) {
        super(Set, source);
        this.elements = new HashSet<>(elements);
    }

    public Set<Expression> getElements() {
        return elements;
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitSetExpr(this);
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
