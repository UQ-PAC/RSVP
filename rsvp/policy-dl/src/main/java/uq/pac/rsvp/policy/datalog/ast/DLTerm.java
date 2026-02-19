package uq.pac.rsvp.policy.datalog.ast;

/**
 * Terms of a relation or a constraint
 */
public abstract class DLTerm extends DLNode {
    public static DLTerm var(String name) {
        return new DLVar(name);
    }

    public static DLTerm lit(String val) {
        return new DLString(val);
    }

    public static DLTerm lit(int val) {
        return new DLNumber(val);
    }

    public static DLTerm lit(double val) {
        return new DLNumber(val);
    }
}
