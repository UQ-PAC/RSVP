package uq.pac.rsvp.policy.datalog.invariant;

import uq.pac.rsvp.policy.ast.schema.ActionDefinition;
import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.EntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.common.*;
import uq.pac.rsvp.policy.datalog.translation.TranslationError;
import uq.pac.rsvp.policy.datalog.util.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

class Typing {
    final static BooleanType TBoolean = new BooleanType();
    final static StringType TString = new StringType();
    final static LongType TLong = new LongType();

    private final Map<String, EntityTypeReference> references;

    Typing () {
        this.references = new HashMap<>();
    }

    CommonTypeDefinition convert(CommonTypeDefinition def) {
        return switch (def) {
            case BooleanType t -> TBoolean;
            case LongType t -> TLong;
            case StringType t -> TString;
            case SetTypeDefinition t -> new SetTypeDefinition(convert(t.getElementType()));
            case RecordTypeDefinition t -> {
                Map<String, CommonTypeDefinition> attrs = t.getAttributes().entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, v -> convert(v.getValue())));
                yield new RecordTypeDefinition(null, attrs);
            }
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
