package uq.pac.rsvp.policy.ast.schema;

import org.junit.jupiter.api.Test;
import uq.pac.rsvp.policy.ast.antlrschema.parser.AntlrSchemaParser;

public class SchemaDifferentialTest {

    @Test
    void test() {
        AntlrSchemaParser.parse("text.txt", SCHEMA_TEXT);
    }

    private static String SCHEMA_TEXT = """
            type FileAttr = String;
            type DirAttr = String;
            type FilePermission = {
                read: Bool,
                write: Bool,
                exec: Bool,
                attr: FileAttr
            };

            type DirPermission = {
                read: Bool,
                write: Bool,
                exec: Bool,
                attr: DirAttr
            };

            type Path = String;
            """;
}
