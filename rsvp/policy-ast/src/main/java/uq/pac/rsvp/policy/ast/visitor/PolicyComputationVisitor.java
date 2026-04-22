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

public interface PolicyComputationVisitor<T> {
    T visitPolicySet(PolicySet policySet);

    T visitPolicy(Policy policy);

    T visitInvariant(Invariant invariant);

    T visitQuantifier(Quantifier quantifier);

    T visitBinaryExpr(BinaryExpression expr);

    T visitCallExpr(CallExpression expr);

    T visitConditionalExpr(ConditionalExpression expr);

    T visitPropertyAccessExpr(PropertyAccessExpression expr);

    T visitRecordExpr(RecordExpression expr);

    T visitSetExpr(SetExpression expr);

    T visitUnaryExpr(UnaryExpression expr);

    T visitVariableExpr(VariableExpression expr);

    T visitActionExpr(ActionExpression expr);

    T visitBooleanExpr(BooleanExpression expr);

    T visitEntityExpr(EntityExpression expr);

    T visitLongExpr(LongExpression expr);

    T visitSlotExpr(SlotExpression expr);

    T visitStringExpr(StringExpression expr);

    T visitTypeExpr(TypeExpression expr);
}
