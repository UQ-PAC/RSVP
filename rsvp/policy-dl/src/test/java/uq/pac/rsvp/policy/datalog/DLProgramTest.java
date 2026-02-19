package uq.pac.rsvp.policy.datalog;

import org.junit.jupiter.api.Test;
import uq.pac.rsvp.datalog.ast.*;
import uq.pac.rsvp.policy.datalog.ast.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DLProgramTest {

    private final static String TEST = """
        // Facts
        .decl edge(x: number, y: number)
        edge(1, 2).
        edge(2, 3).

        // Closure
        .decl path(x: number, y: number)
        .output path(IO=stdout)

        path(x, y) :- edge(x, y).
        path(x, y) :- edge(x, y), edge(y, z).""";

    @Test
    void test() {
        DLProgram.Builder builder = new DLProgram.Builder();

        builder.comment("Facts");
        builder.add(
            new DLRelationDecl("edge", List.of(
                new DLDeclTerm("x", DLType.NUMBER),
                new DLDeclTerm("y", DLType.NUMBER)
        )));

        builder.fact("edge", DLTerm.lit(1), DLTerm.lit(2));
        builder.fact("edge", DLTerm.lit(2), DLTerm.lit(3));
        builder.space();

        builder.comment("Closure");
        builder.add(
            new DLRelationDecl("path", List.of(
                new DLDeclTerm("x", DLType.NUMBER),
                new DLDeclTerm("y", DLType.NUMBER))));
        builder.add(new DLDirective(DLDirective.Kind.OUTPUT, "path", "stdout"));
        builder.space();

        builder.add(new DLRule(
            new DLAtom("path", DLTerm.var("x"), DLTerm.var("y")),
            new DLAtom("edge", DLTerm.var("x"), DLTerm.var("y"))
        ));

        builder.add(new DLRule(
            new DLAtom("path", DLTerm.var("x"), DLTerm.var("y")),
            new DLAtom("edge", DLTerm.var("x"), DLTerm.var("y")),
            new DLAtom("edge", DLTerm.var("y"), DLTerm.var("z"))
        ));

        assertEquals(TEST, builder.build().toString());
    }
}
