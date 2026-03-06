package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.schema.Schema;

import java.util.*;

/**
 * Translation for the entire schema (presently a collection of entity type definitions)
 */
public class TranslationSchema {
    private final Map<String, TranslationEntityDefinition> entityTypes;
    private final Schema schema;

    TranslationSchema(Schema schema) {
        this.schema = schema;
        Map<String, TranslationEntityDefinition> data = new HashMap<>();
        schema.entityTypeNames().stream()
                .map(schema::getEntityType)
                .map(TranslationEntityDefinition::new)
                .forEach(t -> {
                    String tn = t.getName();
                    if (data.containsKey(tn)) {
                        throw new RuntimeException("Duplicate entity type: " + tn);
                    }
                    data.put(tn, t);
                });
        entityTypes = Collections.unmodifiableMap(data);
    }

    public Schema getSchema() {
        return schema;
    }

    public TranslationEntityDefinition getTranslationEntityType(String tn) {
        return entityTypes.get(tn);
    }

    public Collection<TranslationEntityDefinition> getDefinitions() {
        return entityTypes.values();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        entityTypes.forEach((etn, tt) -> {
            sb.append(etn).append('\n');
        });
        return sb.toString();
    }
}
