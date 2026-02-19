package uq.pac.rsvp.policy.datalog.ast;

import java.util.Arrays;
import java.util.List;

import static uq.pac.rsvp.policy.datalog.util.Assertion.require;

public final class DLAtom extends DLRuleExpr {
    private final String name;
    private final List<DLTerm> terms;

    public DLAtom(String name, DLTerm ...terms) {
        this(name, Arrays.stream(terms).toList());
    }

    public DLAtom(String name, List<DLTerm> terms) {
        this.name = name;
        this.terms = terms.stream().toList();
        require(!terms.isEmpty());
    }

    public List<DLTerm> getTerms() {
        return terms;
    }

    DLTerm get(int index) {
        return terms.get(index);
    }

    public int arity() {
        return terms.size();
    }

    public String getName() {
        return name;
    }

    @Override
    protected String stringify() {
        return name + "(" + String.join(", ", terms.stream().map(DLTerm::toString).toList()) + ")";
    }

}
