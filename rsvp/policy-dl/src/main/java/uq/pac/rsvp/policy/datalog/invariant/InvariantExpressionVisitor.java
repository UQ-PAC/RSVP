package uq.pac.rsvp.policy.datalog.invariant;

import uq.pac.rsvp.policy.ast.expr.Expression;

class InvariantExpressionVisitor extends InvariantBaseVisitor<Expression> {

    public InvariantExpressionVisitor() {}

    @Override
    public Expression visitVariableExpr(InvariantParser.VariableExprContext ctx) {
        return null;
    }

    @Override
    public Expression visitConjunctionExpr(InvariantParser.ConjunctionExprContext ctx) {
        return null;
    }

    @Override
    public Expression visitNegationExpr(InvariantParser.NegationExprContext ctx) {
        return null;
    }

    @Override
    public Expression visitDisjunctionExpr(InvariantParser.DisjunctionExprContext ctx) {
        return null;
    }

    @Override
    public Expression visitLiteralExpr(InvariantParser.LiteralExprContext ctx) {
        return null;
    }

    @Override
    public Expression visitPropertyExpr(InvariantParser.PropertyExprContext ctx) {
        return null;
    }

    @Override
    public Expression visitProgram(InvariantParser.ProgramContext ctx) {
        return null;
    }
}
