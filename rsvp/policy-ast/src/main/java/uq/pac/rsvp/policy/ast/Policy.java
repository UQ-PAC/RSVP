package uq.pac.rsvp.policy.ast;

import java.util.*;

import com.google.gson.annotations.SerializedName;

import uq.pac.rsvp.policy.ast.expr.BinaryExpression;
import uq.pac.rsvp.policy.ast.expr.BooleanExpression;
import uq.pac.rsvp.policy.ast.expr.UnaryExpression;
import uq.pac.rsvp.support.SourceLoc;

import uq.pac.rsvp.policy.ast.expr.Expression;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;

import static uq.pac.rsvp.policy.ast.Policy.Effect.Permit;
import static uq.pac.rsvp.policy.ast.expr.BinaryExpression.BinaryOp.And;
import static uq.pac.rsvp.policy.ast.expr.UnaryExpression.UnaryOp.Not;

public class Policy extends Statement {

    public enum Effect {
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
        return effect == Permit;
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
     * <p>
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
        return (effect == Permit ? "permit" : "forbid") + " on: " + condition.toString();
    }

    public static class Builder {
        private Effect effect;
        private Expression condition;
        private String name;
        private final Map<String, String> annotations;
        private SourceLoc location;

        public Builder() {
            this.effect = Permit;
            this.condition = null;
            this.name = null;
            this.annotations = new HashMap<>();
            this.location = SourceLoc.MISSING;
        }

        public Builder and(Expression e) {
            if (e != null) {
                this.condition = condition == null ? e : new BinaryExpression(e, And, condition);
            }
            return this;
        }

        public Builder andNot(Expression e) {
            if (e != null) {
                return and(new UnaryExpression(Not, e));
            }
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder effect(Effect effect) {
            this.effect = effect;
            return this;
        }

        public Builder location(SourceLoc location) {
            this.location = location;
            return this;
        }

        public Builder annotation(String name, String value) {
            annotations.put(name, value);
            return this;
        }

        public Builder annotation(String name) {
            return annotation(name, null);
        }

        public Policy build() {
            Expression cond = condition == null ? new BooleanExpression(true) : condition;
            return new Policy(name, effect, cond, annotations, location);
        }
    }
}
