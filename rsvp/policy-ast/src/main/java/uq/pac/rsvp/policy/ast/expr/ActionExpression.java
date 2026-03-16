package uq.pac.rsvp.policy.ast.expr;

import static uq.pac.rsvp.policy.ast.expr.Expression.ExprType.ActionLiteral;

import uq.pac.rsvp.policy.ast.SourceLoc;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;

public class ActionExpression extends EuidExpression {

    public ActionExpression(String eid, String entityType, SourceLoc source) {
        super(ActionLiteral, eid, entityType, source);
    }

    public ActionExpression(String eid, String entityType) {
        this(eid, entityType, SourceLoc.MISSING);
    }

    // Used by Gson
    @SuppressWarnings("unused")
    private ActionExpression() {
        this(null, null, SourceLoc.MISSING);
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitActionExpr(this);
    }

    @Override
    public <T> T compute(PolicyComputationVisitor<T> visitor) {
        return visitor.visitActionExpr(this);
    }
}
