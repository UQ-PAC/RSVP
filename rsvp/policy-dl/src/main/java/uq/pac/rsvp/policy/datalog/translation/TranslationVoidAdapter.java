package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.datalog.ast.DLRuleExpr;

import java.util.ArrayList;
import java.util.List;

public abstract class TranslationVoidAdapter extends VoidVisitorAdapter {
    protected TranslationSchema schema;
    protected TranslationTyping typing;
    protected List<DLRuleExpr> expressions;

    public TranslationVoidAdapter(TranslationSchema schema, TranslationTyping typing) {
        this.schema = schema;
        this.typing = typing;
        this.expressions = new ArrayList<>();
    }

    protected List<DLRuleExpr> getExpressions() {
        return List.copyOf(expressions);
    }
}
