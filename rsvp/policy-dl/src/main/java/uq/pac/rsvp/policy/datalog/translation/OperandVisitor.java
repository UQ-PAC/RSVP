package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.datalog.ast.*;

import java.util.ArrayList;
import java.util.List;

import static uq.pac.rsvp.policy.ast.expr.VariableExpression.Reference.Context;
import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.AttributeRuleDecl;
import static uq.pac.rsvp.policy.datalog.translation.TranslationError.error;

public class OperandVisitor extends ValueVisitorAdapter<DLTerm> {
    private final List<DLRuleExpr> expressions;

    public OperandVisitor() {
        this.expressions = new ArrayList<>();
    }

    public List<DLRuleExpr> getExpressions() {
        return List.copyOf(expressions);
    }

    @Override
    public DLTerm visitActionExpr(ActionExpression expr) {
        return new DLString(expr.getQualifiedId());
    }

    @Override
    public DLTerm visitEntityExpr(EntityExpression expr) {
        return new DLString(expr.getQualifiedEid());
    }

    @Override
    public DLTerm visitStringExpr(StringExpression expr) {
        return new DLString(expr.getValue());
    }

    @Override
    public DLTerm visitLongExpr(LongExpression expr) {
        return new DLNumber(expr.getValue());
    }

    @Override
    public DLTerm visitPropertyAccessExpr(PropertyAccessExpression expr) {
        DLTerm lhs = expr.getObject().compute(this);
        DLTerm attr = DLTerm.lit(expr.getProperty());
        DLVar rhs = Naming.getDLVar();
        this.expressions.add(new DLAtom(AttributeRuleDecl, lhs, attr, rhs));
        return rhs;
    }

    @Override
    public DLTerm visitVariableExpr(VariableExpression expr) {
        error(expr.getReference() != Context, "Unsupported variable: " + expr.getReference());
        return DLTerm.var(expr.toString());
    }
}
