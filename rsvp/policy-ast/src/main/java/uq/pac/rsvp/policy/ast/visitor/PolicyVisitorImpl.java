package uq.pac.rsvp.policy.ast.visitor;

import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.ast.expr.BinaryExpression;
import uq.pac.rsvp.policy.ast.expr.BooleanExpression;
import uq.pac.rsvp.policy.ast.expr.CallExpression;
import uq.pac.rsvp.policy.ast.expr.ConditionalExpression;
import uq.pac.rsvp.policy.ast.expr.EntityExpression;
import uq.pac.rsvp.policy.ast.expr.Expression;
import uq.pac.rsvp.policy.ast.expr.LongExpression;
import uq.pac.rsvp.policy.ast.expr.PropertyAccessExpression;
import uq.pac.rsvp.policy.ast.expr.RecordExpression;
import uq.pac.rsvp.policy.ast.expr.SetExpression;
import uq.pac.rsvp.policy.ast.expr.SlotExpression;
import uq.pac.rsvp.policy.ast.expr.StringExpression;
import uq.pac.rsvp.policy.ast.expr.UnaryExpression;
import uq.pac.rsvp.policy.ast.expr.VariableExpression;

// Basic visitor implementation. Visits each expression tree in a policy set.
// Subclasses can override some or all methods.
public abstract class PolicyVisitorImpl implements PolicyVisitor {
    public void visitPolicySet(PolicySet policySet) {
        for (Policy policy : policySet) {
            policy.accept(this);
        }
    }

    public void visitPolicy(Policy policy) {
        policy.getCondition().accept(this);
    }

    public void visitBinaryExpr(BinaryExpression expr) {
        expr.getLeft().accept(this);
        expr.getRight().accept(this);
    }

    public void visitCallExpr(CallExpression expr) {
        if (expr.getSelf() != null) {
            expr.getSelf().accept(this);
        }

        for (Expression arg : expr.getArgs()) {
            arg.accept(this);
        }
    }

    public void visitConditionalExpr(ConditionalExpression expr) {
        expr.getCondition().accept(this);
        expr.getThen().accept(this);
        if (expr.getElse() != null) {
            expr.getElse().accept(this);
        }
    }

    public void visitPropertyAccessExpr(PropertyAccessExpression expr) {
        expr.getObject().accept(this);
    }

    public void visitRecordExpr(RecordExpression expr) {
        for (String prop : expr.getPropertyNames()) {
            expr.getProperty(prop).accept(this);
        }
    }

    public void visitSetExpr(SetExpression expr) {
        for (Expression elem : expr.getElements()) {
            elem.accept(this);
        }
    }

    public void visitUnaryExpr(UnaryExpression expr) {
        expr.getExpression().accept(this);
    }

    // Literal expressions
    public void visitVariableExpr(VariableExpression expr) {
    }

    public void visitBooleanExpr(BooleanExpression expr) {
    }

    public void visitEntityExpr(EntityExpression expr) {
    }

    public void visitLongExpr(LongExpression expr) {
    }

    public void visitSlotExpr(SlotExpression expr) {
    }

    public void visitStringExpr(StringExpression expr) {
    }

}
