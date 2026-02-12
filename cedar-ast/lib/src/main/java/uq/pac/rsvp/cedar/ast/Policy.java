package uq.pac.rsvp.cedar.ast;

import com.google.gson.annotations.SerializedName;

import uq.pac.rsvp.cedar.ast.expr.Expression;
import uq.pac.rsvp.cedar.ast.visitor.PolicyVisitor;

public class Policy {

    static enum Effect {
        @SerializedName("permit")
        Permit,

        @SerializedName("forbid")
        Forbid
    }

    public final SourceLoc source;

    private Effect effect;
    private Expression condition;

    public Policy(Effect effect, Expression condition, SourceLoc source) {
        this.effect = effect;
        this.condition = condition;
        this.source = source;
    }

    public boolean isPermit() {
        return effect == Effect.Permit;
    }

    public boolean isForbid() {
        return effect == Effect.Forbid;
    }

    public Expression getCondition() {
        return condition;
    }

    public void accept(PolicyVisitor visitor) {
        visitor.visitPolicy(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(effect == Effect.Permit ? "permit" : "forbid");
        sb.append(" on: ");
        sb.append(condition.toString());

        return sb.toString();
    }

}
