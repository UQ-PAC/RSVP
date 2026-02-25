package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;

public abstract class VoidVisitorAdapter implements PolicyVisitor {

    @Override
    public void visitPolicySet(PolicySet policies) {
        throw new TranslationError("unsupported element" + policies);
    }

    @Override
    public void visitPolicy(Policy policy) {
        throw new TranslationError("unsupported element" + policy);
    }

    @Override
    public void visitBinaryExpr(BinaryExpression expr) {
        throw new TranslationError("unsupported element" + expr);
    }

    @Override
    public void visitPropertyAccessExpr(PropertyAccessExpression expr) {
        throw new TranslationError("unsupported element" + expr);
    }

    @Override
    public void visitCallExpr(CallExpression expr) {
        throw new TranslationError("unsupported element" + expr);
    }

    @Override
    public void visitConditionalExpr(ConditionalExpression expr) {
        throw new TranslationError("unsupported element" + expr);
    }

    @Override
    public void visitRecordExpr(RecordExpression expr) {
        throw new TranslationError("unsupported element" + expr);
    }

    @Override
    public void visitSetExpr(SetExpression expr) {
        throw new TranslationError("unsupported element" + expr);
    }

    @Override
    public void visitUnaryExpr(UnaryExpression expr) {
        throw new TranslationError("unsupported element" + expr);
    }

    @Override
    public void visitVariableExpr(VariableExpression expr) {
        throw new TranslationError("unsupported element" + expr);
    }

    @Override
    public void visitBooleanExpr(BooleanExpression expr) {
        throw new TranslationError("unsupported element" + expr);
    }

    @Override
    public void visitEntityExpr(EntityExpression expr) {
        throw new TranslationError("unsupported element" + expr);
    }

    @Override
    public void visitLongExpr(LongExpression expr) {
        throw new TranslationError("unsupported element" + expr);
    }

    @Override
    public void visitSlotExpr(SlotExpression expr) {
        throw new TranslationError("unsupported element" + expr);
    }

    @Override
    public void visitStringExpr(StringExpression expr) {
        throw new TranslationError("unsupported element" + expr);
    }

    @Override
    public void visitTypeExpr(TypeExpression expr) {
        throw new TranslationError("unsupported element" + expr);
    }
}
