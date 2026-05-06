package uq.pac.rsvp.policy.ast.policy.expr;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import uq.pac.rsvp.support.SourceLoc;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyVisitor;

public class RecordExpression extends Expression {

    private final Map<String, Expression> props;

    public RecordExpression(Map<String, Expression> props, SourceLoc source) {
        super(source);
        this.props = props != null ? Map.copyOf(props) : Collections.emptyMap();
    }

    public RecordExpression(Map<String, Expression> props) {
        this(props, SourceLoc.MISSING);
    }

    public Map<String, Expression> getProperties() {
        return props;
    }

    public Set<String> getPropertyNames() {
        return props.keySet();
    }

    public Expression getProperty(String name) {
        return props.get(name);
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitRecordExpr(this);
    }

    @Override
    public <T> T compute(PolicyComputationVisitor<T> visitor) {
        return visitor.visitRecordExpr(this);
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
