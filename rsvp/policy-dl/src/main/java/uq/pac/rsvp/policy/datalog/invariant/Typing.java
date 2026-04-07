package uq.pac.rsvp.policy.datalog.invariant;

import uq.pac.rsvp.policy.ast.schema.ActionDefinition;
import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.EntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.common.*;
import uq.pac.rsvp.policy.datalog.translation.TranslationError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Typing {
    final static BooleanType BooleanType = new BooleanType();
    final static StringType StringType = new StringType();
    final static LongType LongType = new LongType();
    final static BooleanType TypeOfEntityType = new BooleanType();

    record TypeTest(Function<CommonTypeDefinition, Boolean> test, String expected) { }

    final static TypeTest TBoolean = new TypeTest(t -> t == BooleanType, "Boolean");
    final static TypeTest TLong = new TypeTest(t -> t == LongType, "Long");
    final static TypeTest TString = new TypeTest(t -> t == StringType, "String");
    final static TypeTest TTypeOfEntity = new TypeTest(t -> t == TypeOfEntityType, "Entity");
    final static TypeTest TSet = new TypeTest(t -> t instanceof SetTypeDefinition, "Set<?>");
    final static TypeTest TRecord = new TypeTest(
            t -> t instanceof RecordTypeDefinition, "Record, Entity, Action");
    final static TypeTest TEntityOrAction = new TypeTest(
            t -> isEntity(t) || isAction(t), "Entity, Action");
    final static TypeTest TEntity = new TypeTest(Typing::isEntity, "Entity");
    final static TypeTest TAction = new TypeTest(Typing::isAction, "Action");
    private static boolean isActionName(String name) {
        return name.equals("Action") || name.endsWith("::Action");
    }

    private static boolean isAction(CommonTypeDefinition def) {
        return def instanceof RecordTypeDefinition rec
                && rec.hasName()
                && isActionName(rec.getName());
    }

    private static boolean isEntity(CommonTypeDefinition def) {
        return def instanceof EntityTypeReference ref ||
                def instanceof RecordTypeDefinition rec &&
                rec.hasName() &&
                !isActionName(rec.getName());
    }

    static TypeTest expect(CommonTypeDefinition actual, TypeTest ...tests) {
        for (TypeTest test : tests) {
            if (test.test().apply(actual)) {
                return test;
            }
        }
        String expected = Stream.of(tests).map(TypeTest::expected).toList().toString();
        throw new InvariantValidator.Error("Expected one of %s, got %s", expected, Typing.name(actual));
    }


    static TypeTest expect(CommonTypeDefinition actual, List<TypeTest> tests) {
        return expect(actual, tests.toArray(new TypeTest[0]));
    }

    static TypeTest expectCompatible(CommonTypeDefinition one, CommonTypeDefinition another, TypeTest ...tests) {
        return expect(another,  expect(one, tests));
    }

    static TypeTest expectCompatible(CommonTypeDefinition one, CommonTypeDefinition another, List<TypeTest> tests) {
        return expectCompatible(one, another, tests.toArray(new TypeTest[0]));
    }

    private final Map<String, EntityTypeReference> references;

    Typing () {
        this.references = new HashMap<>();
    }

    CommonTypeDefinition convert(CommonTypeDefinition def) {
        return switch (def) {
            case BooleanType t -> BooleanType;
            case LongType t -> LongType;
            case StringType t -> StringType;
            case SetTypeDefinition t -> new SetTypeDefinition(convert(t.getElementType()));
            case RecordTypeDefinition t -> {
                Map<RecordTypeDefinition.Attribute, CommonTypeDefinition> attrs = t.getAttributes().entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, v -> convert(v.getValue())));
                yield new RecordTypeDefinition(attrs);
            }
            case CommonTypeReference c -> c.getDefinition();
            case EntityTypeReference t -> {
                EntityTypeReference ref = references.get(t.getDefinition().getName());
                if (ref == null) {
                    references.put(t.getName(), t);
                    ref = t;
                }
                yield ref;
            }
            default -> throw new TranslationError("Unsupported type: " + def);
        };
    }

    RecordTypeDefinition convert(EntityTypeDefinition def) {
        RecordTypeDefinition ct = (RecordTypeDefinition) convert(def.getShape());
        return new RecordTypeDefinition(def.getName(), ct.getAttributes());
    }

    RecordTypeDefinition convert(ActionDefinition def) {
        return new RecordTypeDefinition(def.getType(), Map.of());
    }

    static String name(CommonTypeDefinition type) {
        return switch (type) {
            case BooleanType b -> "__cedar::Bool";
            case LongType l -> "__cedar::Long";
            case StringType s -> "__cedar::String";
            case EntityTypeReference r -> r.getDefinition().getName();
            case RecordTypeDefinition r -> r.getName() == null ? "::AnonymousRecord" : r.getName();
            case SetTypeDefinition s -> "Set<" + name(s.getElementType()) + ">";
            default -> throw new AssertionError("Unsupported type: " + type);
        };
    }
}
