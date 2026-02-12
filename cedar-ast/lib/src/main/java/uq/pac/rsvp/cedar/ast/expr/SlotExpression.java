package uq.pac.rsvp.cedar.ast.expr;

import static uq.pac.rsvp.cedar.ast.expr.Expression.ExprType.Slot;

import com.google.gson.annotations.SerializedName;

import uq.pac.rsvp.cedar.ast.SourceLoc;
import uq.pac.rsvp.cedar.ast.visitor.PolicyVisitor;

public class SlotExpression extends Expression {

    public static enum SlotId {
        @SerializedName("principal")
        Principal,

        @SerializedName("resource")
        Resource,
    }

    private SlotId id;

    public SlotExpression(SlotId id, SourceLoc source) {
        super(Slot, source);
        this.id = id;
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
    public String toString() {
        switch (id) {
            case Principal:
                return "&principal";
            case Resource:
                return "&resource";
            default:
                return "&error";
        }
    }
}
