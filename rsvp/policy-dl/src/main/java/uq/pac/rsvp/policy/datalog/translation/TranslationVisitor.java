package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.expr.*;

public class TranslationVisitor extends TranslationVisitorAdapter {

    public TranslationVisitor(TranslationSchema schema) {
        super(schema);
    }

    @Override
    public void visitBinaryExpr(BinaryExpression expr) {
        switch (expr.getOp()) {
            case BinaryExpression.BinaryOp.Is -> {
                TypeExpression type = required(expr.getRight(), TypeExpression.class);
                TranslationType en = schema.getTranslationType(type.getTypeName());
            }
            default -> throw new RuntimeException("unsupported");
        }
        throw new RuntimeException("unsupported");
    }
}
