package uq.pac.rsvp.policy.datalog.ast;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Input/output directive
 * <code>
 *   Directive ::= ('.input' | '.output') IDENT '(' [IDENT = (IDENT | STRING)]* ')'
 * </code>
 */
public abstract class DLDirective extends DLStatement {

    private final DLRuleDecl rule;
    private final String dest;
    private final Map<String, String> properties;

    private static final String DEFAULT_DEST = "file";

    public DLDirective(DLRuleDecl rule, String dest, Map<String, String> properties) {
        this.rule = rule;
        this.dest = dest;
        this.properties = Map.copyOf(properties);
    }

    protected abstract String getKind();

    public DLDirective(DLRuleDecl rule, String dest) {
        this(rule, dest, Collections.emptyMap());
    }

    public DLDirective(DLRuleDecl decl) {
        this(decl, DEFAULT_DEST);
    }

    public DLRuleDecl getRule() {
        return rule;
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
        return ".%s %s(IO=%s%s)".formatted(getKind(), rule.getName(), dest, propStr);
    }
}
