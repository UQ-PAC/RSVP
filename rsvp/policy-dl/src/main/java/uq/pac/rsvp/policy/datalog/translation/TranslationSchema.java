package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.value.EntityTypeName;

import java.util.*;

/**
 * Translation for the entire schema (presently a collection of entity declarations)
 */
public class TranslationSchema {
    private final Map<EntityTypeName, TranslationEntity> schema;

    public TranslationSchema(Collection<TranslationEntity> types) {
        Map<EntityTypeName, TranslationEntity> data = new HashMap<>();
        for (TranslationEntity t : types) {
            EntityTypeName tn = t.getTypeName();
            if (data.containsKey(tn)) {
                throw new RuntimeException("Duplicate entity type: " + tn);
            }
            data.put(tn, t);
        }
        schema = Collections.unmodifiableMap(data);
    }

    public TranslationEntity getTranslationType(String tn) {
        return EntityTypeName.parse(tn).map(schema::get).orElse(null);
    }

    public TranslationEntity getTranslationType(EntityTypeName tn) {
        return schema.get(tn);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        schema.forEach((etn, tt) -> {
            sb.append(etn).append('\n');
        });
        return sb.toString();
    }
}
