package uq.pac.rsvp.policy.ast.antlrschema.parser;

import uq.pac.rsvp.policy.ast.CedarschemaParser;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrBuiltinType;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrRecordType;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrReferenceType;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrSetType;
import uq.pac.rsvp.support.FileSource;

import java.util.HashMap;
import java.util.Map;

class AntlrSchemaTypeVisitor extends AntlrSourceVisitor<AntlrBuiltinType> {

    public AntlrSchemaTypeVisitor(FileSource fs) {
        super(fs);
    }

    @Override
    public AntlrBuiltinType visitPath(CedarschemaParser.PathContext ctx) {
        return new AntlrReferenceType(ctx.getText(), location(ctx));
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
