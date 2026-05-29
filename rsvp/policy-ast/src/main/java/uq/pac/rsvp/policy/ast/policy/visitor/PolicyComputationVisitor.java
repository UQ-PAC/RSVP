package uq.pac.rsvp.policy.ast.policy.visitor;

import uq.pac.rsvp.policy.ast.policy.Invariant;
import uq.pac.rsvp.policy.ast.policy.Policy;
import uq.pac.rsvp.policy.ast.policy.expr.*;
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

    T visitStringExpr(StringExpression expr);

    T visitTypeExpr(TypeExpression expr);

    T visitHasExpr(HasExpression expr);
}
