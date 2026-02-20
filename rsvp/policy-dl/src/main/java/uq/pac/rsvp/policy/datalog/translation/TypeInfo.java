package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.value.EntityTypeName;

import java.util.HashMap;
import java.util.Map;

public class TypeInfo {
    private Map<String, EntityTypeName> types;

    public TypeInfo() {
        this.types = new HashMap<>();
    }

    public void add(String var, EntityTypeName type) {
        types.put(var, type);
    }

    public EntityTypeName get(String var) {
        return types.get(var);
    }
}
