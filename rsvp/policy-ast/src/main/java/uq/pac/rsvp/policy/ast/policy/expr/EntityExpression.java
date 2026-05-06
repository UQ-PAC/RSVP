package uq.pac.rsvp.policy.ast.policy.expr;

import uq.pac.rsvp.support.SourceLoc;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyVisitor;

public class EntityExpression extends EuidExpression {

    public EntityExpression(String eid, String entityType, SourceLoc source) {
        super(eid, entityType, source);
    }

    public EntityExpression(String eid, String entityType) {
        this(eid, entityType, SourceLoc.MISSING);
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitEntityExpr(this);
    }

    @Override
    public <T> T compute(PolicyComputationVisitor<T> visitor) {
        return visitor.visitEntityExpr(this);
    }
}
