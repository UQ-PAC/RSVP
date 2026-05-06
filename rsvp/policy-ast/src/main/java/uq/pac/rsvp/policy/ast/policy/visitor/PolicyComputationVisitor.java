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
import uq.pac.rsvp.policy.ast.policy.expr.SlotExpression;
import uq.pac.rsvp.policy.ast.policy.expr.StringExpression;
import uq.pac.rsvp.policy.ast.policy.expr.TypeExpression;
import uq.pac.rsvp.policy.ast.policy.expr.UnaryExpression;
import uq.pac.rsvp.policy.ast.policy.expr.VariableExpression;
import uq.pac.rsvp.policy.ast.policy.Invariant;
import uq.pac.rsvp.policy.ast.policy.Quantifier;

public interface PolicyComputationVisitor<T> {
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
