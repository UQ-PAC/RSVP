package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.entity.EntityReference;
import uq.pac.rsvp.policy.ast.entity.RecordValue;

import java.util.HashMap;
import java.util.Map;

/**
 * At translation time each record is assigned its own EUID and represented
 * as a pseudo-entity reference. To enable comparison (== or !=) of records
 * these entity values are generated based on entity contents, i.e., records
 * containing the same data should be assigned the same ID. The following map
 * allows tracking such references
 */
public class RecordMap {
    private final Map<RecordValue, EntityReference> values;

    public RecordMap() {
        this.values = new HashMap<>();
    }

    public EntityReference getReference(RecordValue value) {
        EntityReference euid = values.get(value);
        if (euid == null) {
            euid = TranslationConstants.getRandomTmpEUID();
            values.put(value, euid);
        }
        return euid;
    }
}
