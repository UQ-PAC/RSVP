package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.datalog.ast.*;

import java.util.Collection;
import java.util.List;

import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.*;
import static uq.pac.rsvp.policy.datalog.util.Assertion.require;
import static uq.pac.rsvp.policy.datalog.util.Util.required;
import static uq.pac.rsvp.policy.ast.expr.VariableExpression.Reference.*;

/**
 * Main translation visitor converting a Cedar expression to a set of rules
 * <p>
 * The visitor expects (potentially negated) predicate expressions coming from some normal form, i.e.,
 *  - An expression is a predicate boolean expression, there are no conjunctions or disjunctions
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
                new DLAtom(ActionPrincipalRuleDecl, ActionVar, PrincipalVar),
                new DLAtom(ActionResourceRuleDecl, ActionVar, ResourceVar),
                new DLAtom(ActionRuleDecl, ActionVar)));

        exprs.forEach(e -> e.accept(visitor));
        DLAtom atom = new DLAtom(decl.getName(),
                DLTerm.var(Principal.getValue()),
                DLTerm.var(Resource.getValue()),
                DLTerm.var(Action.getValue()));
        return new DLRule(atom, visitor.expressions);
    }

    DLConstraint.Operator getOperator(BinaryExpression.BinaryOp op, boolean negated) {
        DLConstraint.Operator dlOp = switch (op) {
            case Eq -> DLConstraint.Operator.EQ;
            case Neq -> DLConstraint.Operator.NEQ;
            case Less -> DLConstraint.Operator.LT;
            case LessEq -> DLConstraint.Operator.LTE;
            case Greater -> DLConstraint.Operator.GT;
            case GreaterEq -> DLConstraint.Operator.GTE;
            default -> throw new TranslationError("Unsupported operator: " + op);
        };

        if (negated) {
            dlOp = switch (dlOp) {
                case EQ -> DLConstraint.Operator.NEQ;
                case NEQ -> DLConstraint.Operator.EQ;
                case LT -> DLConstraint.Operator.GTE;
                case GT -> DLConstraint.Operator.LTE;
                case GTE -> DLConstraint.Operator.LT;
                case LTE -> DLConstraint.Operator.GT;
            };
        }
        return dlOp;
    }

    @Override
    public void visitBinaryExpr(BinaryExpression expr) {
        typing.update(expr, negated);
        switch (expr.getOp()) {
            case Eq, Neq, Less, LessEq, Greater, GreaterEq -> {
                TranslationOperandVisitor lhs = new TranslationOperandVisitor(schema, typing),
                        rhs = new TranslationOperandVisitor(schema, typing);
                DLTerm lhsOp = expr.getLeft().compute(lhs),
                        rhsOp = expr.getRight().compute(rhs);

                expressions.addAll(lhs.getExpressions());
                expressions.addAll(rhs.getExpressions());
                expressions.add(new DLConstraint(lhsOp, rhsOp, getOperator(expr.getOp(), negated)));
            }
            case BinaryExpression.BinaryOp.Is -> {
                TypeExpression typeExpr = required(expr.getRight(), TypeExpression.class);
                TranslationOperandVisitor lhs = new TranslationOperandVisitor(schema, typing);
                DLTerm var = expr.getLeft().compute(lhs);
                expressions.addAll(lhs.getExpressions());
                String relationName = schema.getTranslationEntityType(typeExpr.getValue())
                        .getEntityRuleDecl()
                        .getName();
                expressions.add(new DLAtom(relationName, negated, var));
            }
            case BinaryExpression.BinaryOp.In -> {
                TranslationOperandVisitor lhs = new TranslationOperandVisitor(schema, typing),
                        rhs = new TranslationOperandVisitor(schema, typing);
                DLTerm lhsOp = expr.getLeft().compute(lhs),
                        rhsOp = expr.getRight().compute(rhs);

                expressions.addAll(lhs.getExpressions());
                expressions.addAll(rhs.getExpressions());
                expressions.add(new DLAtom(ParentOfRuleDecl, negated, rhsOp, lhsOp));
            }
            case And, Or -> throw new TranslationError("Unreachable");
            default -> throw new TranslationError("Unsupported: " + expr.getOp());
        }
    }

    @Override
    public void visitUnaryExpr(UnaryExpression expr) {
        require(!negated);
        switch (expr.getOp()) {
            case Neg -> throw new TranslationError("Unsupported: " + expr);
            case Not -> negated = true;
        }
        expr.getExpression().accept(this);
        negated = false;
    }

    @Override
    public void visitBooleanExpr(BooleanExpression expr) {
        if (!expr.getValue()) {
            expressions.add(makeStandardAtom(NullifiedRequestsRuleDecl));
        }
    }

    @Override
    public void visitPropertyAccessExpr(PropertyAccessExpression expr) {
        StringExpression value = new StringExpression(Boolean.toString(!negated));
        new BinaryExpression(expr, BinaryExpression.BinaryOp.Eq, value).accept(this);
    }
}
