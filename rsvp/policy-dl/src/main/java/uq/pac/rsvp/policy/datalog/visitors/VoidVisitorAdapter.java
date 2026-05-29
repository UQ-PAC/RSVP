package uq.pac.rsvp.policy.datalog.visitors;

import uq.pac.rsvp.policy.ast.policy.Policy;
import uq.pac.rsvp.policy.ast.policy.Invariant;
import uq.pac.rsvp.policy.ast.policy.Quantifier;
import uq.pac.rsvp.policy.ast.policy.expr.*;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyVisitor;
import uq.pac.rsvp.support.error.TranslationError;

public class VoidVisitorAdapter implements PolicyVisitor {

    private void unsupported(Object o) {
        throw new TranslationError(this.getClass().getName() +
                ": unsupported element " + o + " of " + o.getClass().getName());
    }

    @Override
    public void visitPolicy(Policy policy) {
        unsupported(policy);
    }

    @Override
    public void visitInvariant(Invariant invariant) {
        unsupported(invariant);
    }

    @Override
    public void visitQuantifier(Quantifier quantifier) {
        unsupported(quantifier);
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
    public void visitActionExpr(ActionExpression expr) {
        unsupported(expr);
    }

    @Override
    public void visitLongExpr(LongExpression expr) {
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

    @Override
    public void visitHasExpr(HasExpression expr) {
        unsupported(expr);
    }
}
