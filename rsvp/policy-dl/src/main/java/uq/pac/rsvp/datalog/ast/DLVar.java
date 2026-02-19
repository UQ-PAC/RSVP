package uq.pac.rsvp.datalog.ast;

public final class DLVar extends DLTerm {
    private final String name;

    public DLVar(String name) {
        this.name = name;
    }

    @Override
    protected String stringify() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DLVar s) {
            return s.name.equals(name);
        }
        return false;
    }

    public String getName() {
        return name;
    }
}
