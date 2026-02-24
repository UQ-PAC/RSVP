package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.schema.Schema;

import java.util.*;

/**
 * Translation for the entire schema (presently a collection of entity declarations)
 */
public class TranslationSchema {
    private final Map<String, TranslationType> schema;

    private TranslationSchema(Collection<TranslationType> types) {
        Map<String, TranslationType> data = new HashMap<>();
        for (TranslationType t : types) {
            String tn = t.getName();
            if (data.containsKey(tn)) {
                throw new RuntimeException("Duplicate entity type: " + tn);
            }
            data.put(tn, t);
        }
        schema = Collections.unmodifiableMap(data);
    }

    public TranslationType getTranslationType(String tn) {
        return schema.get(tn);
    }

    public Collection<TranslationType> getTranslationTypes() {
        return schema.values();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        schema.forEach((etn, tt) -> {
            sb.append(etn).append('\n');
        });
        return sb.toString();
    }

    // FIXME: Find out what happens to namespaces
    public static TranslationSchema get(Schema schema) {
        List<TranslationType> types = schema.values().stream().flatMap(namespace -> {
                return namespace.entityTypeNames().stream()
                        .map(namespace::getEntityType)
                        .map(entity -> new TranslationType(entity, namespace))
                        .toList()
                        .stream();
        }).toList();
        return new TranslationSchema(types);
    }
}
