package uq.pac.rsvp.policy.ast.schema;

import uq.pac.rsvp.policy.ast.CedarschemaParser;
import uq.pac.rsvp.policy.ast.schema.common.RecordTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.common.SetTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.common.UnresolvedTypeReference;
import uq.pac.rsvp.support.FileSource;

import java.util.HashMap;
import java.util.Map;

class SchemaTypeVisitor extends SourceVisitor<CommonTypeDefinition> {
    public SchemaTypeVisitor(FileSource fs) {
        super(fs);
    }

    @Override
    public CommonTypeDefinition visitNamedType(CedarschemaParser.NamedTypeContext ctx) {
        return new UnresolvedTypeReference(ctx.path().getText());
    }

    public static String unquote(String s) {
        if (s != null && s.length() > 1 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    @Override
    public CommonTypeDefinition visitRecord(CedarschemaParser.RecordContext ctx) {
        Map<String, CommonTypeDefinition> attributes = new HashMap<>();
        ctx.attribute().forEach(a -> {
            CommonTypeDefinition type = a.type().accept(this);
            String attrName = unquote(a.name().getText());
            attributes.put(attrName, type);
        });
        return new RecordTypeDefinition(attributes);
    }

    @Override
    public CommonTypeDefinition visitRecordType(CedarschemaParser.RecordTypeContext ctx) {
        return ctx.record().accept(this);
    }

    @Override
    public CommonTypeDefinition visitSet(CedarschemaParser.SetContext ctx) {
        return new SetTypeDefinition(ctx.type().accept(this));
    }

    @Override
    public CommonTypeDefinition visitSetType(CedarschemaParser.SetTypeContext ctx) {
        return ctx.set().accept(this);
    }
}
