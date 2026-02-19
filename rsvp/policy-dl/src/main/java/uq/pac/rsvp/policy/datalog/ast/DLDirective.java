package uq.pac.rsvp.policy.datalog.ast;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class DLDirective extends DLStatement {
    public enum Kind {
        INPUT("input"),
        OUTPUT("output");

        private final String value;

        Kind(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private final Kind kind;
    private final String relation;
    private final String dest;
    private final Map<String, String> properties;

    public DLDirective(Kind kind, String relation, String dest, Map<String, String> properties) {
        this.kind = kind;
        this.relation = relation;
        this.dest = dest;
        this.properties = Map.copyOf(properties);
    }

    public DLDirective(Kind kind, String relation, String dest) {
        this(kind, relation, dest, Collections.emptyMap());
    }

    public DLDirective(Kind kind, String relation) {
        this(kind, relation, "file");
    }

    public Kind getKind() {
        return kind;
    }

    public String getRelation() {
        return relation;
    }

    public String getDest() {
        return dest;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public String stringify() {
        String propStr = properties.entrySet()
                .stream().map(e -> "%s=\"%s\"".formatted(e.getKey(), e.getValue()))
                .collect(Collectors.joining(","));
        if (!propStr.isEmpty()) {
            propStr = ", " + propStr;
        }
        return ".%s %s(IO=%s%s)".formatted(kind, relation, dest, propStr);
    }
}
