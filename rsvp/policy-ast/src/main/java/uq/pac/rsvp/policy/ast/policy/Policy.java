package uq.pac.rsvp.policy.ast.policy;

import java.util.*;

import uq.pac.rsvp.policy.ast.policy.expr.BinaryExpression;
import uq.pac.rsvp.policy.ast.policy.expr.BooleanExpression;
import uq.pac.rsvp.policy.ast.policy.expr.UnaryExpression;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyPayloadVisitor;
import uq.pac.rsvp.support.SourceLoc;

import uq.pac.rsvp.policy.ast.policy.expr.Expression;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyVisitor;

import static uq.pac.rsvp.policy.ast.policy.Policy.Effect.Permit;
import static uq.pac.rsvp.policy.ast.policy.expr.BinaryExpression.Operator.And;
import static uq.pac.rsvp.policy.ast.policy.expr.UnaryExpression.Operator.Not;
import static uq.pac.rsvp.support.SourceLoc.MISSING;

public class Policy extends PolicyStatement {

    public enum Effect {
        Permit,
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
    public <T, P> T compute(PolicyPayloadVisitor<T, P> visitor, P payload) {
        return visitor.visitPolicy(this, payload);
    }

    @Override
    public String toString() {
        return "%s (principal, action, resource) when { %s };"
                .formatted(effect.toString().toLowerCase(), condition.toString());
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
            this.location = MISSING;
        }

        public Builder and(Expression e, SourceLoc loc) {
            if (e != null) {
                this.condition = condition == null ? e : new BinaryExpression(condition, And, e, loc);
            }
            return this;
        }

        public Builder andNot(Expression e, SourceLoc loc) {
            if (e != null) {
                return and(new UnaryExpression(Not, e, loc), loc);
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

        public Expression condition() {
            return condition;
        }
    }
}
