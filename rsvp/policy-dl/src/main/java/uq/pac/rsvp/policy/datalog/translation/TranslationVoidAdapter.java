package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;
import uq.pac.rsvp.policy.datalog.ast.DLRuleExpr;

import java.util.ArrayList;
import java.util.List;

public abstract class TranslationVoidAdapter implements PolicyVisitor {
    protected TranslationSchema schema;
    protected TranslationTypeInfo typeInfo;
    protected List<DLRuleExpr> expressions;

    public TranslationVoidAdapter(TranslationSchema schema, TranslationTypeInfo typeInfo) {
        this.schema = schema;
        this.typeInfo = typeInfo;
        this.expressions = new ArrayList<>();
    }

    static <E> E required(Object o, Class<E> cls) {
        if (cls.isInstance(o)) {
            return cls.cast(o);
        }
        throw new RuntimeException("Unsupported type: " + o.getClass().getSimpleName());
    }

    @Override
    public void visitPolicySet(PolicySet policySet) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public void visitPolicy(Policy policy) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public void visitBinaryExpr(BinaryExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public void visitPropertyAccessExpr(PropertyAccessExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public void visitCallExpr(CallExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public void visitConditionalExpr(ConditionalExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public void visitRecordExpr(RecordExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public void visitSetExpr(SetExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public void visitUnaryExpr(UnaryExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public void visitVariableExpr(VariableExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public void visitBooleanExpr(BooleanExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public void visitEntityExpr(EntityExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public void visitLongExpr(LongExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public void visitSlotExpr(SlotExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public void visitStringExpr(StringExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public void visitTypeExpr(TypeExpression expr) {
        throw new RuntimeException("unsupported");
    }
}
