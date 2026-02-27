package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.datalog.ast.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static uq.pac.rsvp.policy.datalog.util.Assertion.require;

public class TranslationRule {
    private final DLRuleDecl decl;
    private final List<DLStatement> contents;


    public TranslationRule(DLRuleDecl decl, DLStatement ...statements) {
        this(decl, Arrays.stream(statements).toList());
    }

    public TranslationRule(DLRuleDecl decl, Collection<? extends DLStatement> statements) {
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

    public List<DLStatement> getContents() {
        return contents;
    }

    public List<DLStatement> getStatements() {
        List<DLStatement> statements = new ArrayList<>(contents.size() + 1);
        statements.add(decl);
        statements.addAll(contents);
        return statements;
    }
}
