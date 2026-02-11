package uq.pac.rsvp.ast.visitor;

import uq.pac.rsvp.ast.Policy;
import uq.pac.rsvp.ast.PolicySet;
import uq.pac.rsvp.ast.expr.BinaryExpression;
import uq.pac.rsvp.ast.expr.BooleanExpression;
import uq.pac.rsvp.ast.expr.CallExpression;
import uq.pac.rsvp.ast.expr.ConditionalExpression;
import uq.pac.rsvp.ast.expr.EntityExpression;
import uq.pac.rsvp.ast.expr.LongExpression;
import uq.pac.rsvp.ast.expr.PropertyAccessExpression;
import uq.pac.rsvp.ast.expr.RecordExpression;
import uq.pac.rsvp.ast.expr.SetExpression;
import uq.pac.rsvp.ast.expr.SlotExpression;
import uq.pac.rsvp.ast.expr.StringExpression;
import uq.pac.rsvp.ast.expr.UnaryExpression;
import uq.pac.rsvp.ast.expr.VariableExpression;

public interface PolicyVisitor {
    public void visitPolicySet(PolicySet policySet);

    public void visitPolicy(Policy policy);

    public void visitBinaryExpr(BinaryExpression expr);

    public void visitCallExpr(CallExpression expr);

    public void visitConditionalExpr(ConditionalExpression expr);

    public void visitPropertyAccessExpr(PropertyAccessExpression expr);

    public void visitRecordExpr(RecordExpression expr);

    public void visitSetExpr(SetExpression expr);

    public void visitUnaryExpr(UnaryExpression expr);

    public void visitVariableExpr(VariableExpression expr);

    public void visitBooleanExpr(BooleanExpression expr);

    public void visitEntityExpr(EntityExpression expr);

    public void visitLongExpr(LongExpression expr);

    public void visitSlotExpr(SlotExpression expr);

    public void visitStringExpr(StringExpression expr);

}
