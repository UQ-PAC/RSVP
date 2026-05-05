package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.antlrschema.AntlrSchema;

import java.util.*;

/**
 * Translation for the entire schema as collection of entity type definitions
 */
public class TranslationSchema {
    private final Map<String, TranslationEntityDefinition> entityTypes;
    private final AntlrSchema schema;

    TranslationSchema(AntlrSchema schema) {
        this.schema = schema;
        Map<String, TranslationEntityDefinition> data = new HashMap<>();
        schema.entityTypes()
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

    public AntlrSchema getSchema() {
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
        entityTypes.forEach((etn, tt) -> sb.append(etn).append('\n'));
        return sb.toString();
    }
}
