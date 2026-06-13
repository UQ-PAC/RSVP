package uq.pac.rsvp.policy.datalog.logic;

public abstract class Formula {
    // toString cache
    private String cache = null;

    protected abstract String stringify();

    @Override
    public final String toString() {
        if (cache == null) {
            cache = stringify();
        }
        return cache;
    }

    public abstract <T> T accept(FormulaVisitor<T> visitor);
}
