package uq.pac.rsvp.policy.datalog.ast;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Input directive
 * <code>
 *   Directive ::= '.input' IDENT '(' [IDENT = (IDENT | STRING)]* ')'
 * </code>
 */
public class DLInputDirective extends DLDirective {
    public DLInputDirective(DLRuleDecl relation, String dest, Map<String, String> properties) {
        super(relation, dest, properties);
    }

    public DLInputDirective(DLRuleDecl rule, String dest) {
        super(rule, dest);
    }

    public DLInputDirective(DLRuleDecl decl) {
        super(decl);
    }

    @Override
    protected String getKind() {
        return "input";
    }
}
