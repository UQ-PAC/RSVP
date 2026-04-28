package uq.pac.rsvp.policy.ast.antlrschema.parser;

import uq.pac.rsvp.policy.ast.CedarschemaParser;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrCommonType;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrSchemaStatement;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrBuiltinType;
import uq.pac.rsvp.support.FileSource;

class AntlrSchemaStatementVisitor extends AntlrSourceVisitor<AntlrSchemaStatement> {

    private final AntlrSchemaTypeVisitor types;
    private final String namespace;

    public AntlrSchemaStatementVisitor(FileSource fs, AntlrSchemaTypeVisitor types, String ns) {
        super(fs);
        this.types = types;
        this.namespace = ns;
    }

    @Override
    public AntlrSchemaStatement visitEntity(CedarschemaParser.EntityContext ctx) {
        return null;
    }

    @Override
    public AntlrSchemaStatement visitCommon(CedarschemaParser.CommonContext ctx) {
        AntlrBuiltinType definition = types.visit(ctx.type());
        return new AntlrCommonType(namespace, ctx.typename().getText(), definition, location(ctx));
    }

    @Override
    public AntlrSchemaStatement visitAction(CedarschemaParser.ActionContext ctx) {
        return null;
    }
}
