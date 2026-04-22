package uq.pac.rsvp.policy.datalog.visitors;

import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.ast.invariant.Invariant;
import uq.pac.rsvp.policy.ast.invariant.Quantifier;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.datalog.translation.TranslationError;

public class ValueVisitorAdapter<T> implements PolicyComputationVisitor<T> {

    private T unsupported(Object o) {
        throw new TranslationError("unsupported element: " + o);
    }

    public T compute(Expression e) {
        return e.compute(this);
    }

    @Override
    public T visitPolicySet(PolicySet policies) {
        return unsupported(policies);
    }

    @Override
    public T visitPolicy(Policy policy) {
        return unsupported(policy);
    }

    @Override
    public T visitInvariant(Invariant invariant) {
        return unsupported(invariant);
    }

    @Override
    public T visitQuantifier(Quantifier quantifier) {
        return unsupported(quantifier);
    }

    @Override
    public T visitBinaryExpr(BinaryExpression expr) {
        return unsupported(expr);
    }

    @Override
    public T visitPropertyAccessExpr(PropertyAccessExpression expr) {
        return unsupported(expr);
    }

    @Override
    public T visitCallExpr(CallExpression expr) {
        return unsupported(expr);
    }

    @Override
    public T visitConditionalExpr(ConditionalExpression expr) {
        return unsupported(expr);
    }

    @Override
    public T visitRecordExpr(RecordExpression expr) {
        return unsupported(expr);
    }

    @Override
    public T visitSetExpr(SetExpression expr) {
        return unsupported(expr);
    }

    @Override
    public T visitUnaryExpr(UnaryExpression expr) {
        return unsupported(expr);
    }

    @Override
    public T visitVariableExpr(VariableExpression expr) {
        return unsupported(expr);
    }

    @Override
    public T visitBooleanExpr(BooleanExpression expr) {
        return unsupported(expr);
    }

    @Override
    public T visitEntityExpr(EntityExpression expr) {
        return unsupported(expr);
    }

    @Override
    public T visitActionExpr(ActionExpression expr) {
        return unsupported(expr);
    }

    @Override
    public T visitLongExpr(LongExpression expr) {
        return unsupported(expr);
    }

    @Override
    public T visitSlotExpr(SlotExpression expr) {
        return unsupported(expr);
    }

    @Override
    public T visitStringExpr(StringExpression expr) {
        return unsupported(expr);
    }

    @Override
    public T visitTypeExpr(TypeExpression expr) {
        return unsupported(expr);
    }
}
