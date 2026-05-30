package uq.pac.rsvp.policy.ast.policy.visitor;

import uq.pac.rsvp.policy.ast.policy.Policy;
import uq.pac.rsvp.policy.ast.policy.expr.*;
import uq.pac.rsvp.policy.ast.policy.Invariant;
import uq.pac.rsvp.policy.ast.policy.Quantifier;

// Basic visitor implementation. Visits each expression tree in a policy set.
// Subclasses can override some or all methods.
public abstract class PolicyVisitorAdapter implements PolicyVisitor {

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
    public void visitStringExpr(StringExpression expr) {}

    @Override
    public void visitTypeExpr(TypeExpression expr) {}

    @Override
    public void visitHasExpr(HasExpression expr) {
        expr.getExpression().accept(this);
    }

}
