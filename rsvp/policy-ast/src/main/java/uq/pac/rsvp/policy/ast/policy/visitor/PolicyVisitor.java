package uq.pac.rsvp.policy.ast.policy.visitor;

import uq.pac.rsvp.policy.ast.policy.Policy;
import uq.pac.rsvp.policy.ast.policy.expr.*;
import uq.pac.rsvp.policy.ast.policy.Invariant;
import uq.pac.rsvp.policy.ast.policy.Quantifier;

public interface PolicyVisitor {
    void visitPolicy(Policy policy);

    void visitInvariant(Invariant invariant);

    void visitQuantifier(Quantifier quantifier);

    void visitBinaryExpr(BinaryExpression expr);

    void visitCallExpr(CallExpression expr);

    void visitConditionalExpr(ConditionalExpression expr);

    void visitPropertyAccessExpr(PropertyAccessExpression expr);

    void visitRecordExpr(RecordExpression expr);

    void visitSetExpr(SetExpression expr);

    void visitUnaryExpr(UnaryExpression expr);

    void visitVariableExpr(VariableExpression expr);

    void visitActionExpr(ActionExpression expr);

    void visitBooleanExpr(BooleanExpression expr);

    void visitEntityExpr(EntityExpression expr);

    void visitLongExpr(LongExpression expr);

    void visitStringExpr(StringExpression expr);

    void visitTypeExpr(TypeExpression expr);

    void visitHasExpr(HasExpression expr);
}
