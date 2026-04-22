package uq.pac.rsvp.policy.datalog.ast;

import java.util.Arrays;
import java.util.List;

import static uq.pac.rsvp.Assertion.require;

/**
 * Datalog atom
 * <code>
 *   Atom := IDENT '(' Term [ ',' Term ]* ')'
 * </code>
 */
public class DLAtom extends DLRuleExpr {
    private final DLRuleDecl decl;
    private final List<DLTerm> terms;
    private final boolean negated;

    public DLAtom(DLRuleDecl decl, boolean negated, List<DLTerm> terms) {
        this.decl = decl;
        this.terms = terms.stream().toList();
        this.negated = negated;
        require(!terms.isEmpty());
    }

    public DLAtom(DLRuleDecl decl, List<DLTerm> terms) {
        this(decl, false, terms);
    }

    public DLAtom(DLRuleDecl decl, boolean negate, DLTerm ...terms) {
        this(decl, negate, Arrays.stream(terms).toList());
    }

    public DLAtom(DLRuleDecl decl, DLTerm ...terms) {
        this(decl, false, Arrays.stream(terms).toList());
    }

    public List<DLTerm> getTerms() {
        return terms;
    }

    public DLTerm getTerm(int index) {
        return terms.get(index);
    }

    public int arity() {
        return terms.size();
    }

    public DLRuleDecl getDecl() {
        return decl;
    }

    public String getName() {
        return decl.getName();
    }

    public boolean isNegated() {
        return negated;
    }

    @Override
    protected String stringify() {
        return (isNegated() ? "!" : "") +  decl.getName() +
                "(" + String.join(", ", terms.stream().map(DLTerm::toString).toList()) + ")";
    }
}
