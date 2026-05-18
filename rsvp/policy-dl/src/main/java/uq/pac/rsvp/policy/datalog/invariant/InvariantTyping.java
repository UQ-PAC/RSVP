package uq.pac.rsvp.policy.datalog.invariant;

import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.type.*;
import uq.pac.rsvp.support.error.TranslationError;
import uq.pac.rsvp.support.error.ValidationError;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InvariantTyping {
    final static BooleanType BooleanType = new BooleanType();
    final static StringType StringType = new StringType();
    final static LongType LongType = new LongType();
    final static BooleanType TypeOfEntityType = new BooleanType();

    private final Schema schema;

    public Schema getSchema() {
        return schema;
    }

    public record TypeTest(Function<BuiltinType, Boolean> test, String expected) { }

    final static TypeTest TBoolean = new TypeTest(t -> t.equals(BooleanType), "__cedar::Boolean");
    final static TypeTest TLong = new TypeTest(t -> t.equals(LongType), "__cedar::Long");
    final static TypeTest TString = new TypeTest(t -> t.equals(StringType), "__cedar::String");
    final static TypeTest TTypeOfEntity = new TypeTest(t -> t == TypeOfEntityType, "Entity");
    final static TypeTest TSet = new TypeTest(t -> t instanceof SetType, "Set<?>");
    final static TypeTest TRecord = new TypeTest(
            t -> t instanceof RecordType, "Record, Entity, Action");
    final static TypeTest TEntityOrAction = new TypeTest(
            t -> isEntity(t) || isAction(t), "Entity, Action");
    final static TypeTest TEntity = new TypeTest(InvariantTyping::isEntity, "Entity");
    final static TypeTest TAction = new TypeTest(InvariantTyping::isAction, "Action");

    private static boolean isAction(BuiltinType type) {
        return type instanceof TypeReference ref && ref.getName().equals("Action");
    }

    private static boolean isEntity(BuiltinType type) {
        return type instanceof TypeReference ref && !ref.getName().equals("Action");
    }

    static TypeTest expect(BuiltinType actual, TypeTest ...tests) {
        for (TypeTest test : tests) {
            if (test.test().apply(actual)) {
                return test;
            }
        }
        String expected = Stream.of(tests).map(TypeTest::expected).toList().toString();
        throw new ValidationError("Expected one of %s, got %s".formatted(expected, actual.toString()));
    }


    static TypeTest expect(BuiltinType actual, List<TypeTest> tests) {
        return expect(actual, tests.toArray(new TypeTest[0]));
    }

    static TypeTest expectCompatible(BuiltinType one, BuiltinType another, TypeTest ...tests) {
        return expect(another,  expect(one, tests));
    }

    static TypeTest expectCompatible(BuiltinType one, BuiltinType another, List<TypeTest> tests) {
        return expectCompatible(one, another, tests.toArray(new TypeTest[0]));
    }

    InvariantTyping(Schema schema) {
        this.schema = schema;
    }

    BuiltinType convert(BuiltinType def) {
        return switch (def) {
            case BooleanType t -> BooleanType;
            case LongType t -> LongType;
            case StringType t -> StringType;
            case SetType t -> new SetType(convert(t.getElementType()));
            case RecordType t -> {
                Map<RecordType.Attribute, BuiltinType> attrs = t.getAttributes().entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, v -> convert(v.getValue())));
                yield new RecordType(attrs);
            }
            case TypeReference c -> c;
            default -> throw new TranslationError("Unsupported type: " + def);
        };
    }
}
