package uq.pac.rsvp.policy.ast.policy.expr;

import uq.pac.rsvp.policy.ast.policy.visitor.PolicyPayloadVisitor;
import uq.pac.rsvp.support.SourceLoc;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyVisitor;

public class ActionExpression extends EuidExpression {

    public ActionExpression(String eid, String entityType, SourceLoc source) {
        super(eid, entityType, source);
    }

    public ActionExpression(String eid, String entityType) {
        this(eid, entityType, SourceLoc.MISSING);
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitActionExpr(this);
    }

    @Override
    public <T> T compute(PolicyComputationVisitor<T> visitor) {
        return visitor.visitActionExpr(this);
    }

    @Override
    public <T, P> T compute(PolicyPayloadVisitor<T, P> visitor, P payload) {
        return visitor.visitActionExpr(this, payload);
    }
}
