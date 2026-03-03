package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;

public class VoidVisitorAdapter implements PolicyVisitor {

    private void unsupported(Object o) {
        throw new TranslationError("unsupported element: " + o);
    }

    @Override
    public void visitPolicySet(PolicySet policies) {
        unsupported(policies);
    }

    @Override
    public void visitPolicy(Policy policy) {
        unsupported(policy);
    }

    @Override
    public void visitBinaryExpr(BinaryExpression expr) {
        unsupported(expr);
    }

    @Override
    public void visitPropertyAccessExpr(PropertyAccessExpression expr) {
        unsupported(expr);
    }

    @Override
    public void visitCallExpr(CallExpression expr) {
        unsupported(expr);
    }

    @Override
    public void visitConditionalExpr(ConditionalExpression expr) {
        unsupported(expr);
    }

    @Override
    public void visitRecordExpr(RecordExpression expr) {
        unsupported(expr);
    }

    @Override
    public void visitSetExpr(SetExpression expr) {
        unsupported(expr);
    }

    @Override
    public void visitUnaryExpr(UnaryExpression expr) {
        unsupported(expr);
    }

    @Override
    public void visitVariableExpr(VariableExpression expr) {
        unsupported(expr);
    }

    @Override
    public void visitBooleanExpr(BooleanExpression expr) {
        unsupported(expr);
    }

    @Override
    public void visitEntityExpr(EntityExpression expr) {
        unsupported(expr);
    }

    @Override
    public void visitLongExpr(LongExpression expr) {
        unsupported(expr);
    }

    @Override
    public void visitSlotExpr(SlotExpression expr) {
        unsupported(expr);
    }

    @Override
    public void visitStringExpr(StringExpression expr) {
        unsupported(expr);
    }

    @Override
    public void visitTypeExpr(TypeExpression expr) {
        unsupported(expr);
    }
}
