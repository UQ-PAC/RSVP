package uq.pac.rsvp.datalog.ast;

import java.util.List;

public class DLRelationDecl extends DLStatement {
    private final String name;
    private final List<DLDeclTerm> terms;

    public DLRelationDecl(String name, List<DLDeclTerm> terms) {
        this.name = name;
        this.terms = terms.stream().toList();
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
