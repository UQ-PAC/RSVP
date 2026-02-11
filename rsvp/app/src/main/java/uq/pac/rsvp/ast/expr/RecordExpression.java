package uq.pac.rsvp.ast.expr;

import static uq.pac.rsvp.ast.expr.Expression.ExprType.Record;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import uq.pac.rsvp.ast.SourceLoc;
import uq.pac.rsvp.ast.visitor.PolicyVisitor;;

public class RecordExpression extends Expression {

    private Map<String, Expression> props;

    public RecordExpression(Map<String, Expression> props, SourceLoc source) {
        super(Record, source);
        this.props = new HashMap<>(props);
    }

    public Map<String, Expression> getProperties() {
        return new HashMap<>(props);
    }

    public Set<String> getPropertyNames() {
        return props.keySet();
    }

    public Expression getProperty(String name) {
        return props.get(name);
    }

    public void setProperty(String name, Expression expr) {
        props.put(name, expr);
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitRecordExpr(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        String sep = "";
        for (String prop : getPropertyNames()) {
            boolean quote = !NICE_PROP_NAME.matcher(prop).matches();
            sb.append(sep);
            if (quote)
                sb.append("\"");
            sb.append(prop);
            if (quote)
                sb.append("\"");
            sb.append(": ");
            sb.append(getProperty(prop).toString());
            sep = ", ";
        }
        sb.append(" }");
        return sb.toString();
    }

}
