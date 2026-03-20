package uq.pac.rsvp.policy.datalog.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

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

    public DLRule(DLAtom head, DLRuleExpr ...expressions) {
        require(expressions.length > 0);
        require(!head.isNegated(), "Negated atom in head");
        this.head = head;
        this.body = Arrays.stream(expressions).toList();
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

    public String getName() {
        return head.getName();
    }

    protected String stringify() {
        // Here we want to print inline comments together with actual rule expressions
        // using correct punctuation, which gets a bit tricky considering
        // that a rule may end with comment.
        List<String> elements = new ArrayList<>();

        // Last non-comment element
        DLRuleExpr last = null;
        for (int i = body.size() - 1; i >= 0; i--) {
            if (!(body.get(i) instanceof DLInlineComment)) {
                last = body.get(i);
                break;
            }
        }

        // Add a comma as long as an expression is not a comment of a last functional element
        for (DLRuleExpr e : body) {
            String suffix = "\n    ";
            if (!(e instanceof DLInlineComment) && e != last) {
                suffix = "," + suffix;
            }
            elements.add(e.stringify());
            elements.add(suffix);
        }

        // Remove the NL suffix, except for when it is comment,
        // we do nto want to comment-out the end of sentence
        if (last == body.getLast()) {
            elements.removeLast();
        }
        return head.toString() + " :-\n    " + String.join("", elements) + ".";
    }
}
