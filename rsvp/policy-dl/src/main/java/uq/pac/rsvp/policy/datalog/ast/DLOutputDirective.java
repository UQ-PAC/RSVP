package uq.pac.rsvp.policy.datalog.ast;

import java.util.Map;

/**
 * Output directive
 * <code>
 *   Directive ::= '.output' IDENT '(' [IDENT = (IDENT | STRING)]* ')'
 * </code>
 */
public class DLOutputDirective extends DLDirective {
    public DLOutputDirective(DLRuleDecl relation, String dest, Map<String, String> properties) {
        super(relation, dest, properties);
    }

    public DLOutputDirective(DLRuleDecl rule, String dest) {
        super(rule, dest);
    }

    public DLOutputDirective(DLRuleDecl decl) {
        super(decl);
    }

    @Override
    protected String getKind() {
        return "output";
    }
}
