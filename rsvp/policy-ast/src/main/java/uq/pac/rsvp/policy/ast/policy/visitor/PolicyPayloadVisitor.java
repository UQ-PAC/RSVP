/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.ast.policy.visitor;

import uq.pac.rsvp.policy.ast.policy.Invariant;
import uq.pac.rsvp.policy.ast.policy.Policy;
import uq.pac.rsvp.policy.ast.policy.expr.*;

import static uq.pac.rsvp.policy.ast.AstNode.unsupported;

public interface PolicyPayloadVisitor<T, P> {
    default T visitPolicy(Policy policy, P p) { throw unsupported(policy); }

    default T visitInvariant(Invariant invariant, P p) { throw unsupported(invariant); }

    default T visitBinaryExpr(BinaryExpression expr, P p) { throw unsupported(expr); }

    default T visitCallExpr(CallExpression expr, P p) { throw unsupported(expr); }

    default T visitConditionalExpr(ConditionalExpression expr, P p) { throw unsupported(expr); }

    default T visitPropertyAccessExpr(PropertyAccessExpression expr, P p) { throw unsupported(expr); }

    default T visitRecordExpr(RecordExpression expr, P p) { throw unsupported(expr); }

    default T visitSetExpr(SetExpression expr, P p) { throw unsupported(expr); }

    default T visitUnaryExpr(UnaryExpression expr, P p) { throw unsupported(expr); }

    default T visitVariableExpr(VariableExpression expr, P p) { throw unsupported(expr); }

    default T visitActionExpr(ActionExpression expr, P p) { throw unsupported(expr); }

    default T visitBooleanExpr(BooleanExpression expr, P p) { throw unsupported(expr); }

    default T visitEntityExpr(EntityExpression expr, P p) { throw unsupported(expr); }

    default T visitLongExpr(LongExpression expr, P p) { throw unsupported(expr); }

    default T visitStringExpr(StringExpression expr, P p) { throw unsupported(expr); }

    default T visitTypeExpr(TypeExpression expr, P p) { throw unsupported(expr); }

    default T visitHasExpr(HasExpression expr, P p) { throw unsupported(expr); }

    default T visitIsExpr(IsExpression expr, P p) { throw unsupported(expr); }
}
