package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.datalog.ast.DLRuleExpr;

import java.util.ArrayList;
import java.util.List;

public abstract class TranslationValueAdapter<T> implements PolicyComputationVisitor<T> {
    protected TranslationSchema schema;
    protected List<DLRuleExpr> expressions;

    public TranslationValueAdapter(TranslationSchema schema) {
        this.schema = schema;
        this.expressions = new ArrayList<>();
    }

    protected List<DLRuleExpr> getExpressions() {
        return expressions;
    }

    @Override
    public T visitPolicySet(PolicySet policySet) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public T visitPolicy(Policy policy) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public T visitBinaryExpr(BinaryExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public T visitPropertyAccessExpr(PropertyAccessExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public T visitCallExpr(CallExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public T visitConditionalExpr(ConditionalExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public T visitRecordExpr(RecordExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public T visitSetExpr(SetExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public T visitUnaryExpr(UnaryExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public T visitVariableExpr(VariableExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public T visitBooleanExpr(BooleanExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public T visitEntityExpr(EntityExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public T visitLongExpr(LongExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public T visitSlotExpr(SlotExpression expr) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public T visitStringExpr(StringExpression expr) {
        throw new RuntimeException("unsupported");
    }
}
