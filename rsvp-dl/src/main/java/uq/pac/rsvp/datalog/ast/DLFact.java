package uq.pac.rsvp.datalog.ast;

public class DLFact extends DLStatement {
    private final DLAtom atom;

    public DLFact(DLAtom atom) {
        this.atom = atom;
    }

    protected String stringify() {
        return atom.toString() + ".";
    }

    public DLAtom getAtom() {
        return atom;
    }
}
