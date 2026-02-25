package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.datalog.ast.DLRuleExpr;

import java.util.ArrayList;
import java.util.List;

public abstract class TranslationValueAdapter<T> extends ValueVisitorAdapter<T> {
    protected TranslationSchema schema;
    protected TranslationTyping typing;
    protected List<DLRuleExpr> expressions;

    public TranslationValueAdapter(TranslationSchema schema, TranslationTyping typing) {
        this.schema = schema;
        this.typing = typing;
        this.expressions = new ArrayList<>();
    }

    protected List<DLRuleExpr> getExpressions() {
        return expressions;
    }
}
