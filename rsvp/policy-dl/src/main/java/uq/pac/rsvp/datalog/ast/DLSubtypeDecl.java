package uq.pac.rsvp.datalog.ast;

public class DLSubtypeDecl extends DLTypeDecl {
    private final DLType subtype;
    private final DLType supertype;

    public DLSubtypeDecl(DLType subtype, DLType supertype) {
        this.subtype = subtype;
        this.supertype = supertype;
    }

    @Override
    public String stringify() {
        return ".type " + subtype + " <: " + supertype;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DLSubtypeDecl sub) {
            return subtype.equals(sub.subtype) && supertype.equals(sub.supertype);
        }
        return false;
    }
}
