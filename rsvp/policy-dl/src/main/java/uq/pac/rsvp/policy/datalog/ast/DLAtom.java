package uq.pac.rsvp.policy.datalog.ast;

import java.util.Arrays;
import java.util.List;

import static uq.pac.rsvp.policy.datalog.util.Assertion.require;

/**
 * Datalog atom
 * <code>
 *   Atom := IDENT '(' Term [ ',' Term ]* ')'
 * </code>
 */
public final class DLAtom extends DLRuleExpr {
    private final String name;
    private final List<DLTerm> terms;
    // FIXME: Break atoms into expression/non-expression
    private final boolean negate;

    public DLAtom(String name, boolean negate, List<DLTerm> terms) {
        this.name = name;
        this.terms = terms.stream().toList();
        this.negate = negate;
        require(!terms.isEmpty());
    }

    public DLAtom(DLRuleDecl decl, boolean negate, DLTerm ...terms) {
        this(decl.getName(), negate, Arrays.stream(terms).toList());
    }

    public DLAtom(String name, boolean negate, DLTerm ...terms) {
        this(name, negate, Arrays.stream(terms).toList());
    }

    public DLAtom(DLRuleDecl decl, DLTerm ...terms) {
        this(decl.getName(), Arrays.stream(terms).toList());
    }

    public DLAtom(String name, DLTerm ...terms) {
        this(name, Arrays.stream(terms).toList());
    }

    public DLAtom(String name, List<DLTerm> terms) {
        this(name, false, terms);
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

    public boolean isNegated() {
        return negate;
    }

    @Override
    protected String stringify() {
        return  (negate ? "!" : "") +
                name +
                "(" + String.join(", ", terms.stream().map(DLTerm::toString).toList()) + ")";
    }

}
