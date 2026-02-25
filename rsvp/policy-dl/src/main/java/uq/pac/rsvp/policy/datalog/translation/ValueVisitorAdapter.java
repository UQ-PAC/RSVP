package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;

public abstract class ValueVisitorAdapter<T> implements PolicyComputationVisitor<T> {

    @Override
    public T visitPolicySet(PolicySet policies) {
        throw new TranslationError("unsupported element" + policies);
    }

    @Override
    public T visitPolicy(Policy policy) {
        throw new TranslationError("unsupported element" + policy);
    }

    @Override
    public T visitBinaryExpr(BinaryExpression expr) {
        throw new TranslationError("unsupported element" + expr);
    }

    @Override
    public T visitPropertyAccessExpr(PropertyAccessExpression expr) {
        throw new TranslationError("unsupported element" + expr);
    }

    @Override
    public T visitCallExpr(CallExpression expr) {
        throw new TranslationError("unsupported element" + expr);
    }

    @Override
    public T visitConditionalExpr(ConditionalExpression expr) {
        throw new TranslationError("unsupported element" + expr);
    }

    @Override
    public T visitRecordExpr(RecordExpression expr) {
        throw new TranslationError("unsupported element" + expr);
    }

    @Override
    public T visitSetExpr(SetExpression expr) {
        throw new TranslationError("unsupported element" + expr);
    }

    @Override
    public T visitUnaryExpr(UnaryExpression expr) {
        throw new TranslationError("unsupported element" + expr);
    }

    @Override
    public T visitVariableExpr(VariableExpression expr) {
        throw new TranslationError("unsupported element" + expr);
    }

    @Override
    public T visitBooleanExpr(BooleanExpression expr) {
        throw new TranslationError("unsupported element" + expr);
    }

    @Override
    public T visitEntityExpr(EntityExpression expr) {
        throw new TranslationError("unsupported element" + expr);
    }

    @Override
    public T visitLongExpr(LongExpression expr) {
        throw new TranslationError("unsupported element" + expr);
    }

    @Override
    public T visitSlotExpr(SlotExpression expr) {
        throw new TranslationError("unsupported element" + expr);
    }

    @Override
    public T visitStringExpr(StringExpression expr) {
        throw new TranslationError("unsupported element" + expr);
    }

    @Override
    public T visitTypeExpr(TypeExpression expr) {
        throw new TranslationError("unsupported element" + expr);
    }
}
