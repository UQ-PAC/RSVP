package uq.pac.rsvp.policy.datalog.logic;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static uq.pac.rsvp.Assertion.require;

public abstract class LogicalOperator extends Formula {
    private final List<Formula> formulae;

    public LogicalOperator(Formula ...formulae) {
        require(formulae.length >= 2);
        this.formulae = Arrays.stream(formulae).toList();
    }

    abstract String getStringOperator();

    protected String stringify() {
        return "(" + formulae.stream()
                .map(Formula::toString)
                .collect(Collectors.joining(getStringOperator())) + ")";
    }

    public Formula get(int index) {
        return formulae.get(index);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        } else if (other == this) {
            return true;
        } else if (other instanceof LogicalOperator l) {
            return getClass().equals(l.getClass()) && formulae.equals(l.formulae);
        }
        return false;
    }

    public int arity() {
        return formulae.size();
    }
}
