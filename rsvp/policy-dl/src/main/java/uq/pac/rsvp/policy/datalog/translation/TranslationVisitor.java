package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.datalog.ast.*;

import java.util.Collection;
import java.util.List;

import static uq.pac.rsvp.policy.datalog.util.Assertion.require;
import static uq.pac.rsvp.policy.datalog.util.Util.required;
import static uq.pac.rsvp.policy.ast.expr.VariableExpression.Reference.*;

/**
 * Main translation visitor converting a Cedar expression to a set of rules
 * <p>
 * The visitor expects (potentially negated) predicate expressions coming from some normal form, i.e.,
 *  - There are no conjunctions or disjunctions
 *  - An expression can be negated but once only
 */
public class TranslationVisitor extends TranslationVoidAdapter {

    private boolean negated = false;

    private TranslationVisitor(TranslationSchema schema) {
        super(schema, new TranslationTyping(schema.getSchema()));
    }

    public static DLRule translate(TranslationSchema schema, Collection<Expression> exprs, DLRuleDecl decl) {
        TranslationVisitor visitor = new TranslationVisitor(schema);

        // Ground terms
        visitor.expressions.addAll(List.of(
                new DLAtom(TranslationConstants.PrincipalRuleDecl, TranslationConstants.PrincipalVar),
                new DLAtom(TranslationConstants.ResourceRuleDecl, TranslationConstants.ResourceVar),
                new DLAtom(TranslationConstants.ActionRuleDecl, TranslationConstants.ActionVar)));

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
                TranslationOperandVisitor lhs = new TranslationOperandVisitor(schema, typing),
                        rhs = new TranslationOperandVisitor(schema, typing);
                DLTerm lhsOp = expr.getLeft().compute(lhs),
                        rhsOp = expr.getRight().compute(rhs);

                expressions.addAll(lhs.getExpressions());
                expressions.addAll(rhs.getExpressions());
                DLConstraint.Operator op = negated ?
                        DLConstraint.Operator.NEQ : DLConstraint.Operator.EQ;
                expressions.add(new DLConstraint(lhsOp, rhsOp, op));
            }
            case BinaryExpression.BinaryOp.Is -> {
				// FIXME: Handle negated
                TypeExpression typeExpr = required(expr.getRight(), TypeExpression.class);
                TranslationOperandVisitor lhs = new TranslationOperandVisitor(schema, typing);
                DLTerm var = expr.getLeft().compute(lhs);
                expressions.addAll(lhs.getExpressions());
                String relationName = schema.getTranslationEntityType(typeExpr.getValue())
                        .getEntityRuleDecl()
                        .getName();
                expressions.add(new DLAtom(relationName, var));
            }
            case And, Or -> throw new AssertionError("Unreachable");
            default -> throw new RuntimeException("unsupported: " + expr.getOp());
        }
    }

    @Override
    public void visitUnaryExpr(UnaryExpression expr) {
        require(!negated);
        switch (expr.getOp()) {
            case Neg -> throw new TranslationError("Unsupported");
            case Not -> negated = true;
        }
        expr.getExpression().accept(this);
        negated = false;
    }
}
