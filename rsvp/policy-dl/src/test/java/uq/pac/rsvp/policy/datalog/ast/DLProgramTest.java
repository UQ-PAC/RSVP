package uq.pac.rsvp.policy.datalog.ast;

import org.junit.jupiter.api.Test;
import uq.pac.rsvp.policy.datalog.TestUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Build and test and run a datalog program
 */
public class DLProgramTest {

    private final static String TEST = """
        // Facts
        .decl edge(x: number, y: number)
        edge(1, 2).
        edge(2, 3).

        // Closure
        .decl path(x: number, y: number)
        path(x, y) :-
            edge(x, y).
        path(x, y) :-
            path(x, z),
            path(z, y).
        
        // Output
        .output path""";

    @Test
    void closure() throws IOException, InterruptedException {
        DLProgram.Builder builder = new DLProgram.Builder("closure");

        DLRuleDecl edge = new DLRuleDecl("edge",
                new DLDeclTerm("x", DLType.NUMBER),
                new DLDeclTerm("y", DLType.NUMBER));
        DLRuleDecl path = new DLRuleDecl("path", edge.getDeclTerms());

        builder.comment("Facts")
            .add(edge)
            .fact("edge", DLTerm.lit(1), DLTerm.lit(2))
            .fact("edge", DLTerm.lit(2), DLTerm.lit(3))
            .comment("Closure")
            .add(path)
            .add(new DLRule(
                new DLAtom("path", DLTerm.var("x"), DLTerm.var("y")),
                new DLAtom("edge", DLTerm.var("x"), DLTerm.var("y"))))
            .add(new DLRule(
                new DLAtom("path", DLTerm.var("x"), DLTerm.var("y")),
                new DLAtom("path", DLTerm.var("x"), DLTerm.var("z")),
                new DLAtom("path", DLTerm.var("z"), DLTerm.var("y"))))
            .comment("Output")
            .add(new DLOutputDirective(path));

        DLProgram program = builder.build();
        assertEquals(TEST, program.toString());

        Path dir = Path.of(TestUtil.DLTESTDIR.toString(), "program-test");
        TestUtil.removeDirWithContents(dir);
        assertFalse(Files.exists(dir));
        program.execute(dir);

        assertTrue(Files.isDirectory(dir));
        Path dlFile = Path.of(dir.toString(), program.getName() + ".dl");
        assertTrue(Files.exists(dlFile));
        assertTrue(Files.isRegularFile(dlFile));

        Path pathCSV = Path.of(dir.toString(), "path.csv");
        assertTrue(Files.exists(pathCSV));
        assertTrue(Files.isRegularFile(pathCSV));

        Set<String> lines = new HashSet<>(Files.readAllLines(pathCSV));
        assertEquals(3, lines.size());
        assertTrue(lines.contains("1\t2"));
        assertTrue(lines.contains("1\t3"));
        assertTrue(lines.contains("2\t3"));
    }
}
