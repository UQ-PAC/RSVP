package uq.pac.rsvp.policy.datalog.ast;

/**
 * Input directive
 * <code>
 *   Directive ::= '.input' IDENT
 * </code>
 */
public class DLInputDirective extends DLDirective {
    public DLInputDirective(DLRuleDecl decl) {
        super(decl, "input");
    }
}
