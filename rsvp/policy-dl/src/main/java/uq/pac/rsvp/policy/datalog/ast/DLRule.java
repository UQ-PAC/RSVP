package uq.pac.rsvp.policy.datalog.ast;

import java.util.Arrays;
import java.util.List;

import static uq.pac.rsvp.policy.datalog.util.Assertion.require;

/**
 * Datalog rule
 * <code>
 *   Rule := Atom ':-' RuleExpression [',' RuleExpression] '.'
 * </code>
 */
public class DLRule extends DLStatement {
    private final DLAtom head;
    private final List<DLRuleExpr> body;

    public DLRule(DLAtom head, DLRuleExpr ...exprs) {
        require(exprs.length > 0);
        this.head = head;
        this.body = Arrays.stream(exprs).toList();
    }

    public DLRule(DLAtom head, List<DLRuleExpr> body) {
        this.head = head;
        this.body = body.stream().toList();
    }

    public DLAtom getHead() {
        return head;
    }

    public List<DLRuleExpr> getBody() {
        return body;
    }

    protected String stringify() {
        return head.toString() + " :- " +
                String.join(", ", body.stream().map(DLRuleExpr::toString).toList()) + ".";
    }
}
