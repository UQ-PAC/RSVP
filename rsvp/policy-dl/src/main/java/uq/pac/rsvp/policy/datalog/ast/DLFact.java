package uq.pac.rsvp.policy.datalog.ast;

import static uq.pac.rsvp.policy.datalog.util.Assertion.require;

/**
 * Datalog fact
 * <code>
 *   Fact := Atom '.'
 * </code>
 */
public class DLFact extends DLStatement {
    private final DLAtom atom;

    public DLFact(DLRuleDecl decl, DLTerm ...terms) {
        this.atom = new DLAtom(decl, terms);
        require(!atom.isNegated());
    }

    protected String stringify() {
        return atom + ".";
    }

    public DLAtom getAtom() {
        return atom;
    }

    public String getName() {
        return atom.getName();
    }
}
