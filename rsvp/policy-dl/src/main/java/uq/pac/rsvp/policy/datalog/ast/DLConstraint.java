package uq.pac.rsvp.policy.datalog.ast;

public final class DLConstraint extends DLRuleExpr {
    private final DLTerm lhs;
    private final DLTerm rhs;
    private final Operator operator;

    public enum Operator {
        EQ("="),
        NEQ("!="),
        GT(">"),
        GTE(">="),
        LT("<"),
        LTE("<=");

        private final String value;

        Operator(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public DLConstraint(DLTerm lhs, DLTerm rhs, Operator operator) {
        this.rhs = rhs;
        this.lhs = lhs;
        this.operator = operator;
    }

    @Override
    protected String stringify() {
        return lhs + " " + operator + " " + rhs;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DLConstraint c) {
            return c.lhs.equals(lhs) && c.rhs.equals(rhs) && c.operator.equals(operator);
        }
        return false;
    }
}
