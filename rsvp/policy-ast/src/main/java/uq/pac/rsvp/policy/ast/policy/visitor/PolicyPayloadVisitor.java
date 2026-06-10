package uq.pac.rsvp.policy.ast.policy.visitor;

import uq.pac.rsvp.policy.ast.policy.Invariant;
import uq.pac.rsvp.policy.ast.policy.Policy;
import uq.pac.rsvp.policy.ast.policy.Quantifier;
import uq.pac.rsvp.policy.ast.policy.expr.*;

public interface PolicyPayloadVisitor<T, P> {
    default T visitPolicy(Policy policy, P p) { throw new AssertionError(); }

    default T visitInvariant(Invariant invariant, P p) { throw new AssertionError(); }

    default T visitBinaryExpr(BinaryExpression expr, P p) { throw new AssertionError(); }

    default T visitCallExpr(CallExpression expr, P p) { throw new AssertionError(); }

    default T visitConditionalExpr(ConditionalExpression expr, P p) { throw new AssertionError(); }

    default T visitPropertyAccessExpr(PropertyAccessExpression expr, P p) { throw new AssertionError(); }

    default T visitRecordExpr(RecordExpression expr, P p) { throw new AssertionError(); }

    default T visitSetExpr(SetExpression expr, P p) { throw new AssertionError(); }

    default T visitUnaryExpr(UnaryExpression expr, P p) { throw new AssertionError(); }

    default T visitVariableExpr(VariableExpression expr, P p) { throw new AssertionError(); }

    default T visitActionExpr(ActionExpression expr, P p) { throw new AssertionError(); }

    default T visitBooleanExpr(BooleanExpression expr, P p) { throw new AssertionError(); }

    default T visitEntityExpr(EntityExpression expr, P p) { throw new AssertionError(); }

    default T visitLongExpr(LongExpression expr, P p) { throw new AssertionError(); }

    default T visitStringExpr(StringExpression expr, P p) { throw new AssertionError(); }

    default T visitTypeExpr(TypeExpression expr, P p) { throw new AssertionError(); }

    default T visitHasExpr(HasExpression expr, P p) { throw new AssertionError(); }

    default T visitIsExpr(IsExpression expr, P p) { throw new AssertionError(); }
}
