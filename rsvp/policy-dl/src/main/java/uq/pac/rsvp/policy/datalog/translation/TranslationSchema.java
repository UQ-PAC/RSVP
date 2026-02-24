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

    public static TranslationSchema get(Schema schema) {
        return new TranslationSchema(schema.entityTypeNames()
                        .stream()
                        .map(schema::getEntityType)
                        .map(TranslationType::new)
                        .toList());
    }
}
