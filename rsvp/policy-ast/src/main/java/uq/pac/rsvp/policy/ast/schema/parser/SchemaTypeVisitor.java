package uq.pac.rsvp.policy.ast.schema.parser;

import org.antlr.v4.runtime.RuleContext;
import uq.pac.rsvp.policy.ast.CedarschemaParser;
import uq.pac.rsvp.policy.ast.schema.type.BuiltinType;
import uq.pac.rsvp.policy.ast.schema.type.RecordType;
import uq.pac.rsvp.policy.ast.schema.type.TypeReference;
import uq.pac.rsvp.policy.ast.schema.type.SetType;
import uq.pac.rsvp.support.FileSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

class SchemaTypeVisitor extends SourceVisitor<BuiltinType> {

    private final String namespace;

    public SchemaTypeVisitor(FileSource fs, String namespace) {
        super(fs);
        this.namespace = namespace;
    }

    @Override
    public BuiltinType visitPath(CedarschemaParser.PathContext ctx) {
        String namespace;
        if (ctx.ident().size() == 1) {
            // At this point namespace cannot be determined
            // Ensure this method is not invoked for entity, cation and types definitions
            namespace  = null;
        } else {
            namespace = ctx.ident().subList(0, ctx.ident().size() - 1).stream()
                    .map(RuleContext::getText)
                    .collect(Collectors.joining("::"));
        }
        String name = ctx.ident(ctx.ident().size() - 1).getText();
        return new TypeReference(namespace, name, location(ctx));
    }

    public static String unquote(String s) {
        if (s != null && s.length() > 1 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    @Override
    public BuiltinType visitRecord(CedarschemaParser.RecordContext ctx) {
        Map<RecordType.Attribute, BuiltinType> attributes = new HashMap<>();
        ctx.attribute().forEach(a -> {
            BuiltinType type = a.type().accept(this);
            String attrName = unquote(a.name().getText());
            boolean required = a.OPTIONAL() == null;
            attributes.put(new RecordType.Attribute(attrName, required), type);
        });
        return new RecordType(attributes, location(ctx));
    }

    @Override
    public BuiltinType visitSet(CedarschemaParser.SetContext ctx) {
        return new SetType(ctx.type().accept(this), location(ctx));
    }

}
