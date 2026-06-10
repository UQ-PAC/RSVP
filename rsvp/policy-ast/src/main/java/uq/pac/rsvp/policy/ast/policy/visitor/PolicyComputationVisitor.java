package uq.pac.rsvp.policy.ast.policy.visitor;

import uq.pac.rsvp.policy.ast.policy.Invariant;
import uq.pac.rsvp.policy.ast.policy.Policy;
import uq.pac.rsvp.policy.ast.policy.expr.*;

public interface PolicyComputationVisitor<T> {
    default T visitPolicy(Policy policy) { throw new AssertionError(); }

    default T visitInvariant(Invariant invariant) { throw new AssertionError(); }

    default T visitBinaryExpr(BinaryExpression expr) { throw new AssertionError(); }

    default T visitCallExpr(CallExpression expr) { throw new AssertionError(); }

    default T visitConditionalExpr(ConditionalExpression expr) { throw new AssertionError(); }

    default T visitPropertyAccessExpr(PropertyAccessExpression expr) { throw new AssertionError(); }

    default T visitRecordExpr(RecordExpression expr) { throw new AssertionError(); }

    default T visitSetExpr(SetExpression expr) { throw new AssertionError(); }

    default T visitUnaryExpr(UnaryExpression expr) { throw new AssertionError(); }

    default T visitVariableExpr(VariableExpression expr) { throw new AssertionError(); }

    default T visitActionExpr(ActionExpression expr) { throw new AssertionError(); }

    default T visitBooleanExpr(BooleanExpression expr) { throw new AssertionError(); }

    default T visitEntityExpr(EntityExpression expr) { throw new AssertionError(); }

    default T visitLongExpr(LongExpression expr) { throw new AssertionError(); }

    default T visitStringExpr(StringExpression expr) { throw new AssertionError(); }

    default T visitTypeExpr(TypeExpression expr) { throw new AssertionError(); }

    default T visitHasExpr(HasExpression expr) { throw new AssertionError(); }

    default T visitIsExpr(IsExpression expr) { throw new AssertionError(); }
}
