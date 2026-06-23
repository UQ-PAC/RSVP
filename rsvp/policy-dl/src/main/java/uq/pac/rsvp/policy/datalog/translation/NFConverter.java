/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.policy.expr.*;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.datalog.logic.*;

import java.util.List;

import static uq.pac.rsvp.Assertion.require;
import static uq.pac.rsvp.policy.ast.policy.expr.BinaryExpression.Operator.And;
import static uq.pac.rsvp.policy.ast.policy.expr.BinaryExpression.Operator.Or;
import static uq.pac.rsvp.policy.ast.policy.expr.UnaryExpression.Operator.Not;

/**
 * Converting a Cedar expression to DNF.
 * <p>
 * This conversion assumes that cedar expressions consist of conjunctions,
 * disjunctions and negations of  boolean-valued predicate expressions.
 * <p>
 * Conversion generates a list of lists of expressions, where each expression
 * list is a conjunctive clause. Each expression, in turn, is a potentially
 * negated predicate expression
 */
public class NFConverter implements PolicyComputationVisitor<Formula> {

    private NFConverter() {
    }

    @Override
    public Formula visitBinaryExpr(BinaryExpression expr) {
        return switch (expr.getOperator()) {
            case And ->
                new Conjunction(expr.getLeft().compute(this), expr.getRight().compute(this));
            case Or ->
                new Disjunction(expr.getLeft().compute(this), expr.getRight().compute(this));
            // In Cedar boolean-valued expressions can be compared via, '==' or '!=', that is, it is possible
            // to construct something like '(a && b) == (c || d)'. In this case the expression is re-written to
            // '((a && b) && (c || d)) || (!(a && b) && !(c || d))'
            case Eq -> {
                if (isScalar(expr.getLeft()) && isScalar(expr.getRight())) {
                    yield new Term<>(expr);
                } else {
                    BinaryExpression lhs = new BinaryExpression(expr.getLeft(), And, expr.getRight());
                    Expression e1 = new UnaryExpression(Not, expr.getLeft());
                    Expression e2 = new UnaryExpression(Not, expr.getRight());
                    Expression rhs = new BinaryExpression(e1, And, e2);
                    yield new BinaryExpression(lhs, Or, rhs).compute(this);
                }
            }
            // != operator by this point should have been re-written to ==
            case Neq -> throw new AssertionError("unreachable");
            default -> new Term<>(expr);
        };
    }

    @Override
    public Formula visitCallExpr(CallExpression expr) {
        return new Term<>(expr);
    }

    @Override
    public Formula visitPropertyAccessExpr(PropertyAccessExpression expr) {
        return new Term<>(expr);
    }

    @Override
    public Formula visitUnaryExpr(UnaryExpression expr) {
        return switch (expr.getOperator()) {
            case UnaryExpression.Operator.Not -> new Negation(expr.getExpression().compute(this));
            case UnaryExpression.Operator.Neg -> new Term<>(expr);
        };
    }

    @Override
    public Formula visitHasExpr(HasExpression expr) {
        return new Term<>(expr);
    }

    @Override
    public Formula visitVariableExpr(VariableExpression expr) {
        return new Term<>(expr);
    }

    @Override
    public Formula visitIsExpr(IsExpression expr) {
        return new Term<>(expr);
    }

    @Override
    public Formula visitBooleanExpr(BooleanExpression expr) {
        return Literal.get(expr.getValue());
    }

    private static Expression toExpression(Formula formula) {
        return formula.accept(new FormulaValueVisitor<>() {
            @Override
            public Expression visitLiteral(Literal literal) {
                return new BooleanExpression(literal.asBoolean());
            }

            @Override
            public Expression visitPredicate(Term<?> term) {
                return (Expression) term.getValue();
            }

            @Override
            public Expression visitNegation(Negation negation) {
                return new UnaryExpression(Not, negation.getFormula().accept(this));
            }

            @Override
            public Expression visitConjunction(Conjunction conjunction) {
                throw new AssertionError("Unexpected formula: " + conjunction);
            }

            @Override
            public Expression visitDisjunction(Disjunction disjunction) {
                throw new AssertionError("Unexpected formula: " + disjunction);
            }
        });
    }

    public static List<List<Expression>> toDNF(Expression expr) {
        NFConverter converter = new NFConverter();
        Formula formula = expr.compute(converter);
        List<List<Formula>> dnf = DNFTransformer.getNormalForm(formula);
        return dnf.stream().map(clause -> {
            return clause.stream().map(NFConverter::toExpression).toList();
        }).toList();
    }

    /**
     * Check if a given expression is scalar, i.e., has no logical connections
     * such as disjunction, conjunction or logical negation
     */
    public static boolean isScalar(Expression expr) {
        require(expr != null);

        if (expr instanceof UnaryExpression e && e.getOperator() == Not) {
            expr = e.getExpression();
        }

        return expr.compute(new ExpressionAdapter() {
            @Override
            public Expression visitBinaryExpr(BinaryExpression expr) {
                return (expr.getOperator() == And || expr.getOperator() == Or)  ? null : super.visitBinaryExpr(expr);
            }

            @Override
            public Expression visitUnaryExpr(UnaryExpression expr) {
                return expr.getOperator() == Not ? null : super.visitUnaryExpr(expr);
            }
        }) != null;
    }
}
