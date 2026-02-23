package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.expr.*;

public class TranslationVisitor extends TranslationValueAdapter<Void> {

    private TypeInfo types;

    public TranslationVisitor(TranslationSchema schema, TypeInfo types) {
        super(schema);
        this.types = types;
    }

    @Override
    public Void visitBinaryExpr(BinaryExpression expr) {
        switch (expr.getOp()) {
            case BinaryExpression.BinaryOp.Is -> {
                // FIXME
            }
            default -> throw new RuntimeException("unsupported");
        }
        throw new RuntimeException("unsupported");
    }
}
