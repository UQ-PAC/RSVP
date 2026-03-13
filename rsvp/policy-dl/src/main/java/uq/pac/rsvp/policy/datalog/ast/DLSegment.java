package uq.pac.rsvp.policy.datalog.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static uq.pac.rsvp.policy.datalog.util.Assertion.require;

/**
 * Helper class associating a rule declaration with statements defining that rule
 */
public class DLSegment {
    private final DLRuleDecl decl;
    private final List<DLStatement> contents;

    public DLSegment(DLRuleDecl decl, DLStatement ...statements) {
        this(decl, Arrays.stream(statements).toList());
    }

    public DLSegment(DLRuleDecl decl, Collection<? extends DLStatement> statements) {
        this.decl = decl;
        statements.forEach(s -> {
            DLAtom atom = switch (s) {
                case DLRule rule -> rule.getHead();
                case DLFact fact -> fact.getAtom();
                default ->
                    throw new AssertionError("Unexpected class: " + s.getClass().getSimpleName());
            };
            require(atom.arity() == decl.arity(), "Mismatching arity");
            require(atom.getName().equals(decl.getName()), "Mismatching name");
        });
        this.contents = List.copyOf(statements);
    }

    public DLRuleDecl getDecl() {
        return decl;
    }

    public List<DLStatement> getStatements() {
        List<DLStatement> statements = new ArrayList<>(contents.size() + 1);
        statements.add(decl);
        statements.addAll(contents);
        return statements;
    }
}
