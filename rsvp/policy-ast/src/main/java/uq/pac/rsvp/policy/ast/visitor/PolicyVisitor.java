package uq.pac.rsvp.policy.ast.visitor;

import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.ast.expr.ActionExpression;
import uq.pac.rsvp.policy.ast.expr.BinaryExpression;
import uq.pac.rsvp.policy.ast.expr.BooleanExpression;
import uq.pac.rsvp.policy.ast.expr.CallExpression;
import uq.pac.rsvp.policy.ast.expr.ConditionalExpression;
import uq.pac.rsvp.policy.ast.expr.EntityExpression;
import uq.pac.rsvp.policy.ast.expr.LongExpression;
import uq.pac.rsvp.policy.ast.expr.PropertyAccessExpression;
import uq.pac.rsvp.policy.ast.expr.RecordExpression;
import uq.pac.rsvp.policy.ast.expr.SetExpression;
import uq.pac.rsvp.policy.ast.expr.SlotExpression;
import uq.pac.rsvp.policy.ast.expr.StringExpression;
import uq.pac.rsvp.policy.ast.expr.TypeExpression;
import uq.pac.rsvp.policy.ast.expr.UnaryExpression;
import uq.pac.rsvp.policy.ast.expr.VariableExpression;
import uq.pac.rsvp.policy.ast.invariant.Invariant;
import uq.pac.rsvp.policy.ast.invariant.Quantifier;

public interface PolicyVisitor {
    void visitPolicySet(PolicySet policySet);

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

    void visitSlotExpr(SlotExpression expr);

    void visitStringExpr(StringExpression expr);

    void visitTypeExpr(TypeExpression expr);

}
