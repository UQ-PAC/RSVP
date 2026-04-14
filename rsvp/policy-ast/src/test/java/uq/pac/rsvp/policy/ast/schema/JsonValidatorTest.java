package uq.pac.rsvp.policy.ast.schema;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.junit.jupiter.api.Test;
import uq.pac.rsvp.policy.ast.JsonValidator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonValidatorTest {

    static Gson gson = new Gson();

    boolean validate(String stringSchema, String stringElement) {
        JsonElement schema = gson.fromJson(stringSchema, JsonElement.class);
        JsonElement element = gson.fromJson(stringElement, JsonElement.class);
        return JsonValidator.validate(schema, element);
    }

    void validateOk(String stringSchema, String stringElement) {
        assertTrue(validate(stringSchema, stringElement));
    }

    void validateFail(String stringSchema, String stringElement) {
        assertFalse(validate(stringSchema, stringElement));
    }

    @Test
    void validateTest() {
        String schema  = """
                {
                    "entity" : { "type": "", "id": "" },
                    "attr?" : {
                        "foo" : "",
                        "bar" : 0,
                        "baz" : true,
                        "list" : [ 0 ]
                    }
                }
                """;

        validateOk(schema, """
                { "entity": { "type": "", "id": "" } }
                """);
        validateOk(schema, """
                {
                    "entity" : { "type": "", "id": "" },
                    "attr" : {
                        "foo" : "",
                        "bar" : 0,
                        "baz" : true,
                        "list" : [ 0 ]
                    }
                }
                """);


        // Top-level key difference
        validateFail(schema, """
                { "entitys": { "type": "", "id": "" } }
                """);

        // Empty array
        validateOk(schema, """
                {
                    "entity" : { "type": "", "id": "" },
                    "attr" : {
                        "foo" : "",
                        "bar" : 0,
                        "baz" : true,
                        "list" : [ ]
                    }
                }
                """);

        // Extra key
        validateFail(schema, """
                {
                    "entity" : { "type": "", "id": "" },
                    "tree" : true,
                    "attr" : {
                        "foo" : "",
                        "bar" : 0,
                        "baz" : true,
                        "list" : [ ]
                    }
                }
                """);

        // Extra key
        validateFail(schema, """
                {
                    "entity" : { "type": "", "id": "" },                    
                    "attr" : {
                        "foo" : "",
                        "bar" : 0,
                        "baz" : true,
                        "list" : [ ],
                        "tree" : true
                    }
                }
                """);

        // Inner-key difference
        validateFail(schema, """
                {
                    "entity" : { "type": "", "id": "" },
                    "attr" : {
                        "foos" : "",
                        "bar" : 0,
                        "baz" : true,
                        "list" : [ 0 ]
                    }
                }
                """);
        // Type difference (object)
        validateFail(schema, """
                {
                    "entity" : { "type": "", "id": "" },
                    "attr" : {
                        "foo" : {},
                        "bar" : 0,
                        "baz" : true,
                        "list" : [ 0 ]
                    }
                }
                """);

        // Type difference (array)
        validateFail(schema, """
                {
                    "entity" : { "type": "", "id": "" },
                    "attr" : {
                        "foo" : [],
                        "bar" : 0,
                        "baz" : true,
                        "list" : [ 0 ]
                    }
                }
                """);

        // Array element difference
        validateFail(schema, """
                {
                    "entity" : { "type": "", "id": "" },
                    "attr" : {
                        "foo" : "",
                        "bar" : 0,
                        "baz" : true,
                        "list" : [ true ]
                    }
                }
                """);

        // Array element difference
        validateFail(schema, """
                {
                    "entity" : { "type": "", "id": "" },
                    "attr" : {
                        "foo" : "",
                        "bar" : 0,
                        "baz" : true,
                        "list" : [ {} ]
                    }
                }
                """);
    }

}
