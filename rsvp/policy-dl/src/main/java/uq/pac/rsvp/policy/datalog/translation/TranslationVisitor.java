package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.datalog.ast.*;

import java.util.Collection;

import static uq.pac.rsvp.policy.datalog.util.Util.required;
import static uq.pac.rsvp.policy.ast.expr.VariableExpression.Reference.*;

/**
 * This visitor expects (negated) predicate expressions coming from some normal form, i.e.,
 *  - There are no conjunctions or disjunction
 *  - An expression can be negated but once only
 */
public class TranslationVisitor extends TranslationVoidAdapter {

    private boolean negated = false;

    private TranslationVisitor(TranslationSchema schema) {
        super(schema, new TranslationTyping(schema.getSchema()));
    }

    public static DLRule translate(TranslationSchema schema, Collection<Expression> exprs, DLRuleDecl decl) {
        TranslationVisitor visitor = new TranslationVisitor(schema);
        exprs.forEach(e -> e.accept(visitor));
        DLAtom atom = new DLAtom(decl.getName(),
                DLTerm.var(Principal.getValue()),
                DLTerm.var(Resource.getValue()),
                DLTerm.var(Action.getValue()));
        return new DLRule(atom, visitor.expressions);
    }

    @Override
    public void visitBinaryExpr(BinaryExpression expr) {
        typing.update(expr, negated);
        switch (expr.getOp()) {
            case Eq -> {
                TranslationOperandVisitor lhs = new TranslationOperandVisitor(schema, typing);
                TranslationOperandVisitor rhs = new TranslationOperandVisitor(schema, typing);

                DLTerm lhsOp = expr.getLeft().compute(lhs);
                DLTerm rhsOp = expr.getRight().compute(rhs);

                expressions.addAll(lhs.getExpressions());
                expressions.addAll(rhs.getExpressions());
                expressions.add(new DLConstraint(lhsOp, rhsOp, DLConstraint.Operator.EQ));
            }
            case And, Or -> {
                throw new AssertionError("Unreachable");
            }
            case BinaryExpression.BinaryOp.Is -> {
                TypeExpression typeExpr = required(expr.getRight(), TypeExpression.class);
                TranslationOperandVisitor lhs = new TranslationOperandVisitor(schema, typing);
                DLTerm var = expr.getLeft().compute(lhs);
                expressions.addAll(lhs.getExpressions());
                String relationName = schema.getTranslationType(typeExpr.getValue())
                        .getEntityRuleDecl()
                        .getName();
                expressions.add(new DLAtom(relationName, var));
            }
            default -> throw new RuntimeException("unsupported: " + expr.getOp());
        }
    }

    @Override
    public void visitUnaryExpr(UnaryExpression expr) {
        boolean logic = switch (expr.getOp()) {
            case Neg -> false;
            case Not -> true;
        };
        if (logic) {
            negated = true;
        }
        expr.getExpression().accept(this);
        if (logic) {
            negated = false;
        }
    }
}
