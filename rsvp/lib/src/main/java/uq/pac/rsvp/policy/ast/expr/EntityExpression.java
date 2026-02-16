package uq.pac.rsvp.policy.ast.expr;

import static uq.pac.rsvp.policy.ast.expr.Expression.ExprType.EntityLiteral;

import java.util.ArrayList;
import java.util.List;

import uq.pac.rsvp.policy.ast.SourceLoc;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;;

public class EntityExpression extends Expression {

    private List<String> path;
    private String eid;

    public EntityExpression(String eid, List<String> path, SourceLoc source) {
        super(EntityLiteral, source);
        this.path = path;
        this.eid = eid;
    }

    public String getEid() {
        return eid;
    }

    public List<String> getQualifiedEid() {
        List<String> result = new ArrayList<>(path);
        result.add(eid);
        return result;
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitEntityExpr(this);
    }

    @Override
    public String toString() {
        return String.join("::", path) + "::\"" + eid + "\"";
    }

}
