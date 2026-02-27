package uq.pac.rsvp.policy.datalog.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Rule declaration
 * <code>
 *  RuleDeclaration ::= '.decl' IDENT '(' DeclTerm [ ',' DeclTerm ]* ')'
 * </code>
 */
public class DLRuleDecl extends DLStatement {
    private final String name;
    private final List<DLDeclTerm> terms;

    public DLRuleDecl(String name, List<DLDeclTerm> terms) {
        this.name = name;
        this.terms = List.copyOf(terms);
    }

    public DLRuleDecl(String name, DLType ...types) {
        this.name = name;
        if (types.length > 26) {
            throw new RuntimeException("Size exceeds allowed limit");
        }
        List<DLDeclTerm> terms = new ArrayList<>(types.length);
        for (int i = 0; i < types.length; i++) {
            terms.add(new DLDeclTerm(Character.toString('a' + i), types[i]));
        }
        this.terms = List.copyOf(terms);
    }

    public DLRuleDecl(String name, DLDeclTerm ...terms) {
        this(name, Arrays.stream(terms).toList());
    }

    public String getName() {
        return name;
    }

    public List<DLDeclTerm> getTerms() {
        return terms;
    }

    public int arity() {
        return getTerms().size();
    }

    @Override
    protected String stringify() {
        return ".decl " + name + "(" +
                String.join(", ", terms.stream().map(DLDeclTerm::toString).toList()) + ")";
    }
}
