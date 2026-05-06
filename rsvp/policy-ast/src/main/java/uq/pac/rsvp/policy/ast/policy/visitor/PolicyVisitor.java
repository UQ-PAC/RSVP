package uq.pac.rsvp.policy.ast.policy.visitor;

import uq.pac.rsvp.policy.ast.policy.Policy;
import uq.pac.rsvp.policy.ast.policy.expr.ActionExpression;
import uq.pac.rsvp.policy.ast.policy.expr.BinaryExpression;
import uq.pac.rsvp.policy.ast.policy.expr.BooleanExpression;
import uq.pac.rsvp.policy.ast.policy.expr.CallExpression;
import uq.pac.rsvp.policy.ast.policy.expr.ConditionalExpression;
import uq.pac.rsvp.policy.ast.policy.expr.EntityExpression;
import uq.pac.rsvp.policy.ast.policy.expr.LongExpression;
import uq.pac.rsvp.policy.ast.policy.expr.PropertyAccessExpression;
import uq.pac.rsvp.policy.ast.policy.expr.RecordExpression;
import uq.pac.rsvp.policy.ast.policy.expr.SetExpression;
import uq.pac.rsvp.policy.ast.policy.expr.StringExpression;
import uq.pac.rsvp.policy.ast.policy.expr.TypeExpression;
import uq.pac.rsvp.policy.ast.policy.expr.UnaryExpression;
import uq.pac.rsvp.policy.ast.policy.expr.VariableExpression;
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

}
