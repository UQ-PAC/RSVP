package uq.pac.rsvp.policy.ast.schema;

import org.junit.jupiter.api.Test;
import uq.pac.rsvp.policy.ast.antlrschema.AntlrSchema;
import uq.pac.rsvp.policy.ast.antlrschema.parser.AntlrSchemaParser;

public class SchemaDifferentialTest {

    @Test
    void test() {
        AntlrSchema schema = AntlrSchemaParser.parse("text.txt", SCHEMA_TEXT);
        System.out.println(schema);
    }

    private final static String SCHEMA_TEXT = """
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
            
            namespace Bar {
                entity Super;
            }
            
            namespace Foo {
                type Path = String;

                entity Role enum [ "A", "B" ];

                entity Account in Bar::Super = {
                    name: String,
                    attr: {
                        read: Bool
                    }
                };
            }
            """;
}
