package uq.pac.rsvp.policy.datalog.ast;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DLType extends DLNode {
    private static final Set<String> RESERVED_TYPENAMES = Set.of(
            "number",
            "symbol",
            "unsigned",
            "float");

    private static final Map<String, DLType> RESERVED_TYPES;
    static {
        RESERVED_TYPES = new HashMap<>();
        for (String tn : RESERVED_TYPENAMES) {
            RESERVED_TYPES.put(tn, new DLType(tn));
        }
    }

    public static DLType NUMBER = RESERVED_TYPES.get("number");
    public static DLType SYMBOL = RESERVED_TYPES.get("symbol");

    private final String name;

    public String getName() {
        return name;
    }

    private DLType(String name) {
        this.name = name;
    }

    public static DLType get(String name) {
        return RESERVED_TYPES.getOrDefault(name, new DLType(name));
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DLType t) {
            return t.name.equals(name);
        }
        return false;
    }

    @Override
    public String stringify() {
        return name;
    }
}
