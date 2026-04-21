package uq.pac.rsvp.policy.ast.expr;

import uq.pac.rsvp.support.SourceLoc;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;;

public class EntityExpression extends EuidExpression {

    public EntityExpression(String eid, String entityType, SourceLoc source) {
        super(eid, entityType, source);
    }

    public EntityExpression(String eid, String entityType) {
        this(eid, entityType, SourceLoc.MISSING);
    }

    // Used by Gson
    @SuppressWarnings("unused")
    private EntityExpression() {
        this(null, null, SourceLoc.MISSING);
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
