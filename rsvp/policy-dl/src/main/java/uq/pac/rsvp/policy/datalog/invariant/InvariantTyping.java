package uq.pac.rsvp.policy.datalog.invariant;

import uq.pac.rsvp.policy.ast.antlrschema.AntlrSchema;
import uq.pac.rsvp.policy.ast.antlrschema.type.*;
import uq.pac.rsvp.policy.datalog.translation.TranslationError;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InvariantTyping {
    final static AntlrBooleanType BooleanType = new AntlrBooleanType();
    final static AntlrStringType StringType = new AntlrStringType();
    final static AntlrLongType LongType = new AntlrLongType();
    final static AntlrBooleanType TypeOfEntityType = new AntlrBooleanType();

    private final AntlrSchema schema;

    public AntlrSchema getSchema() {
        return schema;
    }

    public record TypeTest(Function<AntlrBuiltinType, Boolean> test, String expected) { }

    final static TypeTest TBoolean = new TypeTest(t -> t.equals(BooleanType), "__cedar::Boolean");
    final static TypeTest TLong = new TypeTest(t -> t.equals(LongType), "__cedar::Long");
    final static TypeTest TString = new TypeTest(t -> t.equals(StringType), "__cedar::String");
    final static TypeTest TTypeOfEntity = new TypeTest(t -> t == TypeOfEntityType, "Entity");
    final static TypeTest TSet = new TypeTest(t -> t instanceof AntlrSetType, "Set<?>");
    final static TypeTest TRecord = new TypeTest(
            t -> t instanceof AntlrRecordType, "Record, Entity, Action");
    final static TypeTest TEntityOrAction = new TypeTest(
            t -> isEntity(t) || isAction(t), "Entity, Action");
    final static TypeTest TEntity = new TypeTest(InvariantTyping::isEntity, "Entity");
    final static TypeTest TAction = new TypeTest(InvariantTyping::isAction, "Action");

    private static boolean isAction(AntlrBuiltinType type) {
        return type instanceof AntlrTypeReference ref && ref.getName().equals("Action");
    }

    private static boolean isEntity(AntlrBuiltinType type) {
        return type instanceof AntlrTypeReference ref && !ref.getName().equals("Action");
    }

    static TypeTest expect(AntlrBuiltinType actual, TypeTest ...tests) {
        for (TypeTest test : tests) {
            if (test.test().apply(actual)) {
                return test;
            }
        }
        String expected = Stream.of(tests).map(TypeTest::expected).toList().toString();
        throw new InvariantValidator.Error("Expected one of %s, got %s", expected, actual.toString());
    }


    static TypeTest expect(AntlrBuiltinType actual, List<TypeTest> tests) {
        return expect(actual, tests.toArray(new TypeTest[0]));
    }

    static TypeTest expectCompatible(AntlrBuiltinType one, AntlrBuiltinType another, TypeTest ...tests) {
        return expect(another,  expect(one, tests));
    }

    static TypeTest expectCompatible(AntlrBuiltinType one, AntlrBuiltinType another, List<TypeTest> tests) {
        return expectCompatible(one, another, tests.toArray(new TypeTest[0]));
    }

    InvariantTyping(AntlrSchema schema) {
        this.schema = schema;
    }

    AntlrBuiltinType convert(AntlrBuiltinType def) {
        return switch (def) {
            case AntlrBooleanType t -> BooleanType;
            case AntlrLongType t -> LongType;
            case AntlrStringType t -> StringType;
            case AntlrSetType t -> new AntlrSetType(convert(t.getElementType()));
            case AntlrRecordType t -> {
                Map<AntlrRecordType.Attribute, AntlrBuiltinType> attrs = t.getAttributes().entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, v -> convert(v.getValue())));
                yield new AntlrRecordType(attrs);
            }
            case AntlrTypeReference c -> c;
            default -> throw new TranslationError("Unsupported type: " + def);
        };
    }
}
