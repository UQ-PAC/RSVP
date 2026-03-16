package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.datalog.ast.*;

/**
 * At the datalog level the translation keeps pretty every bit of information as strings.
 * This works for most cases, but to accommodate cedar integer operations symbolic type
 * needs to be converted to numeric using built-in to_number functor of souffle.
 * This visitor determines symbolic or numeric context and provides functionality to
 * add functors based on the context.
 *
 * FIXME: For the moment strings cab be mixed up with datalog entity names at the datalog level
 *        One way aroud it is to add unique prefixes, say 'uid:' for entity identifiers and
 *        'str:' for strings. These prefixes can further be extended to capture types
 */
public class TypeContextVisitor extends ValueVisitorAdapter<TypeContextVisitor.Context> {

    public enum Context {
        NUMERIC, SYMBOLIC, UNDEFINED
    }

    private final static TypeContextVisitor VISITOR = new TypeContextVisitor();

    private TypeContextVisitor() { }

    public static Context infer(Expression expr) {
        Context context = expr.compute(VISITOR);
        return context == Context.UNDEFINED ? Context.SYMBOLIC : context;
    }

    public static DLTerm normalise(DLTerm term, Context context) {
        if (term instanceof DLVar var && context == Context.NUMERIC) {
            return new DLFunctor(DLFunctor.Functor.TO_NUMBER, var);
        }
        return term;
    }

    private Context merge(Context lhs, Context rhs) {
        if (rhs == lhs || lhs == Context.UNDEFINED) {
            return rhs;
        } else if (rhs == Context.UNDEFINED) {
            return lhs;
        }
        throw new TranslationError("Conflicting contexts: " + lhs + " / " + rhs);
    }

    @Override
    public Context visitBinaryExpr(BinaryExpression expr) {
        Context lhsContext = expr.getLeft().compute(this),
                rhsContext = expr.getRight().compute(this);
        Context operandContext = merge(lhsContext, rhsContext);
        Context operatorContext = switch (expr.getOp()) {
            case Add, Mul, Sub, Greater, GreaterEq, Less, LessEq -> Context.NUMERIC;
            case Eq, Neq -> Context.UNDEFINED;
            default -> Context.SYMBOLIC;
        };
        return merge(operandContext, operatorContext);
    }

    @Override
    public Context visitUnaryExpr(UnaryExpression expr) {
        Context operatorContext = switch (expr.getOp()) {
            case Neg -> Context.NUMERIC;
            case Not -> Context.SYMBOLIC;
        };
        Context operandContext = expr.getExpression().compute(this);
        return merge(operandContext, operatorContext);
    }

    @Override
    public Context visitPropertyAccessExpr(PropertyAccessExpression expr) {
        return Context.UNDEFINED;
    }

    @Override
    public Context visitSetExpr(SetExpression expr) {
        return Context.UNDEFINED;
    }

    @Override
    public Context visitVariableExpr(VariableExpression expr) {
        return Context.SYMBOLIC;
    }

    @Override
    public Context visitActionExpr(ActionExpression expr) {
        return Context.SYMBOLIC;
    }

    @Override
    public Context visitBooleanExpr(BooleanExpression expr) {
        return Context.SYMBOLIC;
    }

    @Override
    public Context visitEntityExpr(EntityExpression expr) {
        return Context.SYMBOLIC;
    }

    @Override
    public Context visitLongExpr(LongExpression expr) {
        return Context.NUMERIC;
    }

    @Override
    public Context visitStringExpr(StringExpression expr) {
        return Context.SYMBOLIC;
    }

    @Override
    public Context visitTypeExpr(TypeExpression expr) {
        return Context.SYMBOLIC;
    }
}
