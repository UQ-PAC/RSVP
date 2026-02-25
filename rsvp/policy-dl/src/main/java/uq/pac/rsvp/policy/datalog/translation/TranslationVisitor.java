package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.datalog.ast.DLAtom;
import uq.pac.rsvp.policy.datalog.ast.DLConstraint;
import uq.pac.rsvp.policy.datalog.ast.DLTerm;

import java.util.Set;

import static uq.pac.rsvp.policy.datalog.util.Util.required;

public class TranslationVisitor extends TranslationVoidAdapter {

    public TranslationVisitor(TranslationSchema schema, TranslationTyping typeInfo) {
        super(schema, typeInfo);
    }

    @Override
    public void visitBinaryExpr(BinaryExpression expr) {
        typing.update(expr);
        switch (expr.getOp()) {
            case BinaryExpression.BinaryOp.Eq -> {
                TranslationOperandVisitor lhs = new TranslationOperandVisitor(schema, typing);
                TranslationOperandVisitor rhs = new TranslationOperandVisitor(schema, typing);

                DLTerm lhsOp = expr.getLeft().compute(lhs);
                DLTerm rhsOp = expr.getRight().compute(rhs);

                expressions.addAll(lhs.getExpressions());
                expressions.addAll(rhs.getExpressions());
                expressions.add(new DLConstraint(lhsOp, rhsOp, DLConstraint.Operator.EQ));
            }
            case BinaryExpression.BinaryOp.And -> {
                expr.getLeft().accept(this);
                expr.getRight().accept(this);
            }
            case BinaryExpression.BinaryOp.Is -> {
                TypeExpression typeExpr = required(expr.getRight(), TypeExpression.class);
                TranslationOperandVisitor lhs = new TranslationOperandVisitor(schema, typing);
                DLTerm var = expr.getLeft().compute(lhs);
                expressions.addAll(lhs.getExpressions());
                String relationName = schema.getTranslationType(typeExpr.getValue())
                        .getEntityRelation()
                        .getName();
                expressions.add(new DLAtom(relationName, var));
            }
            default -> throw new RuntimeException("unsupported: " + expr.getOp());
        }
    }
}
