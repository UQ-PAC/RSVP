package uq.pac.rsvp.policy.ast.antlrschema.statement;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class AntlrAnnotations {
    private final Map<String, String> annotations;

    private AntlrAnnotations(Map<String, String> annotations) {
        this.annotations = Collections.unmodifiableMap(annotations);
    }

    public static class Builder {
        private final Map<String, String> annotations;

        public Builder() {
            this.annotations = new LinkedHashMap<>();
        }

        public Builder add(String key, String value) {
            value = value == null ? "" : value;
            annotations.put(key, value);
            return this;
        }

        public AntlrAnnotations build() {
            return new AntlrAnnotations(annotations);
        }
    }

    public Map<String, String> asMap() {
        return annotations;
    }

    public String get(String key) {
        return annotations.get(key);
    }

    public Set<String> getAnnotations() {
        return annotations.keySet();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        asMap().forEach((k, v) -> {
            sb.append('@').append(k);
            if (!v.isEmpty()) {
                sb.append("(\"").append(v).append("\")");
            }
            sb.append('\n');
        });
        return sb.toString();
    }
}
