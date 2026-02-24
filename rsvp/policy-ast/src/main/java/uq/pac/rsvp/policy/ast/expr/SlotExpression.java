package uq.pac.rsvp.policy.ast.expr;

import static uq.pac.rsvp.policy.ast.expr.Expression.ExprType.Slot;

import com.google.gson.annotations.SerializedName;

import uq.pac.rsvp.policy.ast.SourceLoc;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;

public class SlotExpression extends Expression {

    public static enum SlotId {
        @SerializedName("principal")
        Principal,

        @SerializedName("resource")
        Resource,
    }

    private final SlotId id;

    public SlotExpression(SlotId id, SourceLoc source) {
        super(Slot, source);
        this.id = id;
    }

    public SlotExpression(SlotId id) {
        this(id, SourceLoc.MISSING);
    }

    public boolean isPrincipalSlot() {
        return id == SlotId.Principal;
    }

    public boolean isResourceSlot() {
        return id == SlotId.Resource;
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitSlotExpr(this);
    }

    @Override
    public <T> T compute(PolicyComputationVisitor<T> visitor) {
        return visitor.visitSlotExpr(this);
    }

    @Override
    public String toString() {
        return switch (id) {
            case Principal -> "&principal";
            case Resource -> "&resource";
        };
    }
}
