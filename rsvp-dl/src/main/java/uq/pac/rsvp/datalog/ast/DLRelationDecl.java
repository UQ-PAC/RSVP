package uq.pac.rsvp.datalog.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DLRelationDecl extends DLStatement {
    private final String name;
    private final List<DLDeclTerm> terms;

    public DLRelationDecl(String name, List<DLDeclTerm> terms) {
        this.name = name;
        this.terms = List.copyOf(terms);
    }

    public DLRelationDecl(String name, DLType ...types) {
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

    public DLRelationDecl(String name, DLDeclTerm ...terms) {
        this(name, Arrays.stream(terms).toList());
    }

    public String getName() {
        return name;
    }

    public List<DLDeclTerm> getTerms() {
        return terms;
    }

    @Override
    protected String stringify() {
        return ".decl " + name + "(" +
                String.join(", ", terms.stream().map(DLDeclTerm::toString).toList()) + ")";
    }
}
