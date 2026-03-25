package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.datalog.ast.*;
import uq.pac.rsvp.policy.datalog.invariant.Quantifier;

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
    /**
     * Translation schema (entity definitions alongside with Datalog declarations)
     */
    private final TranslationSchema schema;
    /**
     * A list of generated expressions forming the body of the generated rule
     */
    private final List<DLRuleExpr> expressions;
    /**
     * Computing operands. A separated visitor generating side effects
     * when inferring Datalog variables
     */
    private final OperandVisitor operandVisitor;
    /**
     * {@code true} if the provided expression is negated
     */
    private boolean negated = false;
    /**
     * Context of the translation (i.e., whether the translation is for policies or for invariants)
     */
    private final TranslationContext context;

    private TranslationVisitor(TranslationSchema schema, TranslationContext context) {
        this.schema = schema;
        this.expressions = new ArrayList<>();
        this.operandVisitor = new OperandVisitor(this);
        this.context = context;
    }

    TranslationContext getContext() {
        return context;
    }

    public static DLRule translatePolicy(TranslationSchema schema, Collection<Expression> exprs, DLRuleDecl decl) {
        TranslationVisitor visitor = new TranslationVisitor(schema, TranslationContext.Policy);

        // Ground terms. When translating policies the generated expressions
        // are over principals, resources and actions. The boundaries of the above
        // are obtained from the limits declared in actions (all actions declare
        // which principals and resources they apply to)
        visitor.expressions.add(new DLInlineComment("Ground terms"));
        visitor.expressions.addAll(List.of(
                new DLAtom(ActionPrincipalRuleDecl, ActionVar, PrincipalVar),
                new DLAtom(ActionResourceRuleDecl, ActionVar, ResourceVar)));

        exprs.forEach(e -> {
            visitor.expressions.add(new DLInlineComment(e.toString()));
            e.accept(visitor);
        });
        return new DLRule(makeAtom(decl), visitor.expressions);
    }

    public static DLRule translateInvariant(TranslationSchema schema, Collection<Expression> exprs, DLRuleDecl decl, Quantifier quantifier) {
        TranslationVisitor visitor = new TranslationVisitor(schema, TranslationContext.Invariant);

        // Grounding terms for typed variables of is straightforward in that
        // each variable belongs to the entity relation defined by its type
        visitor.expressions.add(new DLInlineComment("Ground terms"));
        quantifier.variables().forEach(var -> {
            String type = quantifier.getType(var);
            DLRuleDecl entityDecl = schema.getTranslationEntityType(type).getEntityRuleDecl();
            visitor.expressions.add(new DLAtom(entityDecl, DLTerm.var(var)));
        });

        exprs.forEach(e -> {
            visitor.expressions.add(new DLInlineComment(e.toString()));
            e.accept(visitor);
        });
        return new DLRule(makeAtom(decl), visitor.expressions);
    }

    void addExpression(DLRuleExpr expr) {
        expressions.add(expr);
    }

    private DLTerm getOperand(Expression expr) {
        return expr.compute(operandVisitor);
    }

    /**
     * Translation between cedar to souffle operators in presence of negation
     */
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
                expressions.add(new DLAtom(HasAttributeRuleDecl, negated, lhs, rhs));
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
            expressions.add(makeAtom(NullifiedRequestsRuleDecl));
        }
    }

    @Override
    public void visitPropertyAccessExpr(PropertyAccessExpression expr) {
        StringExpression value = new StringExpression(Boolean.toString(!negated));
        new BinaryExpression(expr, BinaryExpression.BinaryOp.Eq, value).accept(this);
    }

    @Override
    public void visitCallExpr(CallExpression expr) {
        TranslationFunctions.Function function = TranslationFunctions.getFunction(expr.getFunc(), context);
        if (function != null) {
            List<DLRuleExpr> exprs = function.translate(expr, negated, operandVisitor);
            expressions.addAll(exprs);
        } else {
            throw new TranslationError("Unsupported function: " + expr.getFunc());
        }
    }
}
