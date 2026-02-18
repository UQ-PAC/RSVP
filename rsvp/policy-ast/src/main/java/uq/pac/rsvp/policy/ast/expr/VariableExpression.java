package uq.pac.rsvp.policy.ast.expr;

import static uq.pac.rsvp.policy.ast.expr.Expression.ExprType.Variable;

import com.google.gson.annotations.SerializedName;

import uq.pac.rsvp.policy.ast.SourceLoc;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;

public class VariableExpression extends Expression {

    public static enum Reference {

        @SerializedName("principal")
        Principal,

        @SerializedName("resource")
        Resource,

        @SerializedName("action")
        Action,

        @SerializedName("context")
        Context
    }

    private Reference ref;

    public VariableExpression(Reference ref, SourceLoc source) {
        super(Variable, source);
        this.ref = ref;
    }

    public static VariableExpression createPrincipalRef(SourceLoc source) {
        return new VariableExpression(Reference.Principal, source);
    }

    public static VariableExpression createResourceRef(SourceLoc source) {
        return new VariableExpression(Reference.Resource, source);
    }

    public static VariableExpression createActionRef(SourceLoc source) {
        return new VariableExpression(Reference.Action, source);
    }

    public static VariableExpression createContextRef(SourceLoc source) {
        return new VariableExpression(Reference.Context, source);
    }

    public Reference getReference() {
        return ref;
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitVariableExpr(this);
    }

    @Override
    public <T> T compute(PolicyComputationVisitor<T> visitor) {
        return visitor.visitVariableExpr(this);
    }

    @Override
    public String toString() {
        switch (ref) {
            case Action:
                return "action";
            case Context:
                return "context";
            case Principal:
                return "principal";
            case Resource:
                return "resource";
            default:
                return "error";
        }
    }
}
