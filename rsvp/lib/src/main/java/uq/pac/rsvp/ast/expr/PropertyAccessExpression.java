package uq.pac.rsvp.ast.expr;

import static uq.pac.rsvp.ast.expr.Expression.ExprType.PropertyAccess;

import uq.pac.rsvp.ast.SourceLoc;
import uq.pac.rsvp.ast.visitor.PolicyVisitor;

public class PropertyAccessExpression extends Expression {

    private Expression obj;
    private String prop;

    public PropertyAccessExpression(Expression obj, String prop, SourceLoc source) {
        super(PropertyAccess, source);
        this.obj = obj;
        this.prop = prop;
    }

    public Expression getObject() {
        return obj;
    }

    public String getProperty() {
        return prop;
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitPropertyAccessExpr(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(obj.toString());

        if (NICE_PROP_NAME.matcher(prop).matches()) {
            sb.append('.');
            sb.append(prop);
        } else {
            sb.append("[\"");
            sb.append(prop);
            sb.append("\"]");
        }

        return sb.toString();
    }

}
