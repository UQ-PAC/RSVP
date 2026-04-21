package uq.pac.rsvp.policy.ast;

import java.util.Collections;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

import uq.pac.rsvp.support.SourceLoc;

import uq.pac.rsvp.policy.ast.expr.Expression;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;

public class Policy extends AstNode implements PolicyItem {

    static enum Effect {
        @SerializedName("permit")
        Permit,

        @SerializedName("forbid")
        Forbid
    }

    private final String name;
    private final Effect effect;
    private final Expression condition;
    private final Map<String, String> annotations;

    public Policy(String name, Effect effect, Expression condition, Map<String, String> annotations, SourceLoc source) {
        super(source);
        this.name = name;
        this.effect = effect;
        this.condition = condition;
        this.annotations = annotations != null ? Map.copyOf(annotations) : Collections.emptyMap();
    }

    public Policy(Effect effect, Expression condition, Map<String, String> annotations) {
        this(null, effect, condition, annotations, SourceLoc.MISSING);
    }

    public Policy(String name, Effect effect, Expression condition) {
        this(name, effect, condition, null, SourceLoc.MISSING);
    }

    public Policy(Effect effect, Expression condition) {
        this(null, effect, condition, null, SourceLoc.MISSING);
    }

    public Policy() {
        this(null, Effect.Forbid, null, null, SourceLoc.MISSING);
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

    /**
     * Get the name that uniquely identifies this policy. If this policy was parsed
     * from a Cedar formatted policy file, this name will be the identifier assigned
     * by Cedar and should correspond to any Cedar log messages. If this policy was
     * created manually, the name may be {@code null}.
     *
     * Names are not currently checked for uniqueness. If constructed manually, it
     * is possible for more than one policy to have the same name.
     * 
     * @return the unique name of this policy, or null.
     */
    public String getName() {
        return name;
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
        return (effect == Effect.Permit ? "permit" : "forbid") + " on: " + condition.toString();
    }

}
