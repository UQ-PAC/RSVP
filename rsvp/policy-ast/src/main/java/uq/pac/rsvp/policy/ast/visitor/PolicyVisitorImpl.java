package uq.pac.rsvp.policy.ast.visitor;

import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.expr.ActionExpression;
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
import uq.pac.rsvp.policy.ast.expr.TypeExpression;
import uq.pac.rsvp.policy.ast.expr.UnaryExpression;
import uq.pac.rsvp.policy.ast.expr.VariableExpression;
import uq.pac.rsvp.policy.ast.invariant.Invariant;
import uq.pac.rsvp.policy.ast.invariant.Quantifier;

// Basic visitor implementation. Visits each expression tree in a policy set.
// Subclasses can override some or all methods.
public abstract class PolicyVisitorImpl implements PolicyVisitor {

    @Override
    public void visitPolicy(Policy policy) {
        policy.getCondition().accept(this);
    }

    @Override
    public void visitInvariant(Invariant invariant) {
        invariant.getExpression().accept(this);
        invariant.getQuantifier().accept(this);
    }

    @Override
    public void visitQuantifier(Quantifier quantifier) {
        quantifier.getVariables().forEach(v -> {
            v.name().accept(this);
            v.type().accept(this);
        });
    }

    @Override
    public void visitBinaryExpr(BinaryExpression expr) {
        expr.getLeft().accept(this);
        expr.getRight().accept(this);
    }

    @Override
    public void visitCallExpr(CallExpression expr) {
        if (expr.getSelf() != null) {
            expr.getSelf().accept(this);
        }

        for (Expression arg : expr.getArgs()) {
            arg.accept(this);
        }
    }

    @Override
    public void visitConditionalExpr(ConditionalExpression expr) {
        expr.getCondition().accept(this);
        expr.getThen().accept(this);
        if (expr.getElse() != null) {
            expr.getElse().accept(this);
        }
    }

    @Override
    public void visitPropertyAccessExpr(PropertyAccessExpression expr) {
        expr.getObject().accept(this);
    }

    @Override
    public void visitRecordExpr(RecordExpression expr) {
        for (String prop : expr.getPropertyNames()) {
            expr.getProperty(prop).accept(this);
        }
    }

    @Override
    public void visitSetExpr(SetExpression expr) {
        for (Expression elem : expr.getElements()) {
            elem.accept(this);
        }
    }

    @Override
    public void visitUnaryExpr(UnaryExpression expr) {
        expr.getExpression().accept(this);
    }

    // Literal expressions
    @Override
    public void visitVariableExpr(VariableExpression expr) {}

    @Override
    public void visitActionExpr(ActionExpression expr) {}

    @Override
    public void visitBooleanExpr(BooleanExpression expr) {}

    @Override
    public void visitEntityExpr(EntityExpression expr) {}

    @Override
    public void visitLongExpr(LongExpression expr) {}

    @Override
    public void visitSlotExpr(SlotExpression expr) {}

    @Override
    public void visitStringExpr(StringExpression expr) {}

    @Override
    public void visitTypeExpr(TypeExpression expr) {}

}
