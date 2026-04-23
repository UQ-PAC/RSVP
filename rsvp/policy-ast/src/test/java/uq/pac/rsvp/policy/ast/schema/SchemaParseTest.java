package uq.pac.rsvp.policy.ast.schema;

import com.cedarpolicy.model.exception.InternalException;
import com.cedarpolicy.model.schema.Schema;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uq.pac.rsvp.policy.ast.parser.SchemaParser;

public class SchemaParseTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "entity Foo;",
        "entity Foo, Bar;",
        "entity Foo {};",
        "entity Foo { name: String };",

    })
    void test(String text) throws InternalException {

        Schema schema = Schema.parse(Schema.JsonOrCedar.Cedar, text);
        System.out.println(schema);

        String result = SchemaParser.parse("test.cedarschema", text);
        System.out.println(result);
    }
}
