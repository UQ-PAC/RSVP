package uq.pac.rsvp.policy.datalog.ast;

/**
 * Datalog fact
 * <code>
 *   Fact := Atom '.'
 * </code>
 */
public class DLFact extends DLStatement {
    private final DLAtom atom;

    public DLFact(DLAtom atom) {
        this.atom = atom;
    }

    public DLFact(String relation, DLTerm ...terms) {
        this.atom = new DLAtom(relation, terms);
    }

    public DLFact(DLRuleDecl decl, DLTerm ...terms) {
        this.atom = new DLAtom(decl.getName(), terms);
    }

    protected String stringify() {
        return atom.toString() + ".";
    }

    public DLAtom getAtom() {
        return atom;
    }

    public String getRelationName() {
        return atom.getName();
    }
}
