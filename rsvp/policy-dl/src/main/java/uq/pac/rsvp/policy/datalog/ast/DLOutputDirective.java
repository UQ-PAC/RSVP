package uq.pac.rsvp.policy.datalog.ast;

/**
 * Output directive
 * <code>
 *   Directive ::= '.output' IDENT
 * </code>
 */
public class DLOutputDirective extends DLDirective {
    public DLOutputDirective(DLRuleDecl decl) {
        super(decl, "output");
    }
}
