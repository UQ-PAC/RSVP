package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.datalog.ast.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.*;
import static uq.pac.rsvp.policy.datalog.util.Assertion.require;
import static uq.pac.rsvp.policy.datalog.util.Util.required;

/**
 * Main translation visitor converting a Cedar expression to a set of rules
 * <p>
 * The visitor expects (potentially negated) predicate expressions coming from some normal form, i.e.,
 *  - An expression is a predicate boolean expression, there are no conjunctions or disjunctions
 *  - An expression can be negated but once only
 */
public class TranslationVisitor extends VoidVisitorAdapter {
    private final TranslationSchema schema;
    private final List<DLRuleExpr> expressions;
    private final OperandVisitor operandVisitor;
    private boolean negated = false;

    private TranslationVisitor(TranslationSchema schema) {
        this.schema = schema;
        this.expressions = new ArrayList<>();
        this.operandVisitor = new OperandVisitor();
    }

    public static DLRule translate(TranslationSchema schema, Collection<Expression> exprs, DLRuleDecl decl) {
        TranslationVisitor visitor = new TranslationVisitor(schema);
        exprs.forEach(e -> {
            visitor.expressions.add(new DLInlineComment(e.toString()));
            e.accept(visitor);
        });

        // Ground terms
        visitor.expressions.add(new DLInlineComment("Ground terms"));
        visitor.expressions.addAll(List.of(
                new DLAtom(ActionPrincipalRuleDecl, ActionVar, PrincipalVar),
                new DLAtom(ActionResourceRuleDecl, ActionVar, ResourceVar),
                new DLAtom(ActionRuleDecl, ActionVar)));

        // Add side effects from operands
        visitor.expressions.addAll(visitor.operandVisitor.getExpressions());
        return new DLRule(makeStandardAtom(decl), visitor.expressions);
    }

    private DLTerm getOperand(Expression expr) {
        return expr.compute(operandVisitor);
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
        switch (expr.getOp()) {
            case Eq, Neq, Less, LessEq, Greater, GreaterEq -> {
                TypeContextVisitor.Context context = TypeContextVisitor.infer(expr);
                DLTerm lhs = TypeContextVisitor.normalise(getOperand(expr.getLeft()), context),
                        rhs = TypeContextVisitor.normalise(getOperand(expr.getRight()), context);
                expressions.add(new DLConstraint(lhs, rhs, getOperator(expr.getOp(), negated)));
            }
            case Is -> {
                TypeExpression typeExpr = required(expr.getRight(), TypeExpression.class);
                DLTerm var = getOperand(expr.getLeft());
                DLRuleDecl decl = schema.getTranslationEntityType(typeExpr.getValue()).getEntityRuleDecl();
                expressions.add(new DLAtom(decl, negated, var));
            }
            case In -> {
                DLTerm lhs = getOperand(expr.getLeft()),
                        rhs = getOperand(expr.getRight());
                expressions.add(new DLAtom(ParentOfRuleDecl, negated, rhs, lhs));
            }
            case HasAttr -> {
                DLTerm lhs = getOperand(expr.getLeft()),
                    rhs = getOperand(expr.getRight());
                expressions.add(new DLAtom(AttributeRuleDecl, lhs, rhs, DLTerm.var("_")));
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

    @Override
    public void visitCallExpr(CallExpression expr) {
        switch (expr.getFunc()) {
            case "contains" -> {
                require(expr.getArgs().size() == 1);
                DLTerm argument = getOperand(expr.getArgs().getFirst());

                if (expr.getSelf() instanceof PropertyAccessExpression pe) {
                    DLTerm set = getOperand(pe.getObject());
                    DLTerm aggregate = new DLAggregate(DLAggregate.Aggregate.COUNT,
                            new DLAtom(AttributeRuleDecl, set, DLTerm.lit(pe.getProperty()), argument));
                    expressions.add(new DLConstraint(aggregate, DLTerm.lit(1), DLConstraint.Operator.GTE));
                } else {
                    throw new TranslationError("Unsupported set.contains() form: " + expr);
                }

            }
            default -> throw new TranslationError("Unsupported function: " + expr.getFunc());
        }
    }
}
