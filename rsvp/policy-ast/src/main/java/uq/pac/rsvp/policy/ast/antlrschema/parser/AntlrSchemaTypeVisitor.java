package uq.pac.rsvp.policy.ast.antlrschema.parser;

import org.antlr.v4.runtime.RuleContext;
import uq.pac.rsvp.policy.ast.CedarschemaParser;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrBuiltinType;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrRecordType;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrTypeReference;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrSetType;
import uq.pac.rsvp.support.FileSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

class AntlrSchemaTypeVisitor extends AntlrSourceVisitor<AntlrBuiltinType> {

    private final String namespace;

    public AntlrSchemaTypeVisitor(FileSource fs, String namespace) {
        super(fs);
        this.namespace = namespace;
    }

    @Override
    public AntlrBuiltinType visitPath(CedarschemaParser.PathContext ctx) {
        String namespace;
        if (ctx.ident().size() == 1) {
            namespace = this.namespace;
        } else {
            namespace = ctx.ident().subList(0, ctx.ident().size() - 1).stream()
                    .map(RuleContext::getText)
                    .collect(Collectors.joining("::"));
        }
        String name = ctx.ident(ctx.ident().size() - 1).getText();
        return new AntlrTypeReference(namespace, name, location(ctx));
    }

    public static String unquote(String s) {
        if (s != null && s.length() > 1 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    @Override
    public AntlrBuiltinType visitRecord(CedarschemaParser.RecordContext ctx) {
        Map<AntlrRecordType.Attribute, AntlrBuiltinType> attributes = new HashMap<>();
        ctx.attribute().forEach(a -> {
            AntlrBuiltinType type = a.type().accept(this);
            String attrName = unquote(a.name().getText());
            attributes.put(new AntlrRecordType.Attribute(attrName), type);
        });
        return new AntlrRecordType(attributes, location(ctx));
    }

    @Override
    public AntlrBuiltinType visitSet(CedarschemaParser.SetContext ctx) {
        return new AntlrSetType(ctx.type().accept(this), location(ctx));
    }

}
