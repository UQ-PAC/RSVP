package uq.pac.rsvp.policy.ast.visitor;

import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicySet;
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
import uq.pac.rsvp.policy.ast.expr.UnaryExpression;
import uq.pac.rsvp.policy.ast.expr.VariableExpression;

public interface PolicyComputationVisitor<T> {
    public T visitPolicySet(PolicySet policySet);

    public T visitPolicy(Policy policy);

    public T visitBinaryExpr(BinaryExpression expr);

    public T visitCallExpr(CallExpression expr);

    public T visitConditionalExpr(ConditionalExpression expr);

    public T visitPropertyAccessExpr(PropertyAccessExpression expr);

    public T visitRecordExpr(RecordExpression expr);

    public T visitSetExpr(SetExpression expr);

    public T visitUnaryExpr(UnaryExpression expr);

    public T visitVariableExpr(VariableExpression expr);

    public T visitBooleanExpr(BooleanExpression expr);

    public T visitEntityExpr(EntityExpression expr);

    public T visitLongExpr(LongExpression expr);

    public T visitSlotExpr(SlotExpression expr);

    public T visitStringExpr(StringExpression expr);
}
