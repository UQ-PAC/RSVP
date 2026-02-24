package uq.pac.rsvp.policy.ast;

import java.util.Collections;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

import uq.pac.rsvp.policy.ast.expr.Expression;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;

public class Policy extends PolicyFileEntry {

    static enum Effect {
        @SerializedName("permit")
        Permit,

        @SerializedName("forbid")
        Forbid
    }

    private final Effect effect;
    private final Expression condition;
    private final Map<String, String> annotations;

    public Policy(Effect effect, Expression condition, Map<String, String> annotations, SourceLoc source) {
        super(source);
        this.effect = effect;
        this.condition = condition;
        this.annotations = annotations != null ? Map.copyOf(annotations) : Collections.emptyMap();
    }

    public Policy(Effect effect, Expression condition, Map<String, String> annotations) {
        this(effect, condition, annotations, SourceLoc.MISSING);
    }

    public Policy(Effect effect, Expression condition) {
        this(effect, condition, null, SourceLoc.MISSING);
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

    public Map<String, String> getAnnotations() {
        return annotations;
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitPolicy(this);
    }

    @Override
    public <T> T compute(PolicyComputationVisitor<T> visitor) {
        return visitor.visitPolicy(this);
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
