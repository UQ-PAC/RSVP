package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.value.EntityTypeName;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TranslationSchema {
    private final Map<EntityTypeName, TranslationType> schema;

    public TranslationSchema(Collection<TranslationType> types) {
        Map<EntityTypeName, TranslationType> data = new HashMap<>();
        for (TranslationType t : types) {
            EntityTypeName tn = t.getTypeName();
            if (data.containsKey(tn)) {
                throw new RuntimeException("Duplicate entity type: " + tn);
            }
            data.put(tn, t);
        }
        schema = Collections.unmodifiableMap(data);
    }

    public TranslationType getTranslationType(EntityTypeName tn) {
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
