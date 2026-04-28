package uq.pac.rsvp.policy.ast.antlrschema.parser;

import uq.pac.rsvp.policy.ast.CedarschemaParser;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrCommonType;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrEnumEntityType;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrRecordEntityType;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrSchemaStatement;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrBuiltinType;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrRecordType;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrTypeReference;
import uq.pac.rsvp.support.FileSource;
import uq.pac.rsvp.support.SourceLoc;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static uq.pac.rsvp.Assertion.require;

class AntlrSchemaStatementVisitor extends AntlrSourceVisitor<AntlrSchemaStatement> {

    private final AntlrSchemaTypeVisitor types;
    private final String namespace;

    public AntlrSchemaStatementVisitor(FileSource fs, String namespace) {
        super(fs);
        this.namespace = namespace;
        this.types = new AntlrSchemaTypeVisitor(fs, namespace);
    }

    @Override
    public AntlrSchemaStatement visitEntity(CedarschemaParser.EntityContext ctx) {
        require(ctx.entityNames().ID().size() == 1);
        String name = ctx.entityNames().ID(0).getText();

        if (ctx.strings() != null) {
            Set<String> names = ctx.strings().STRING().stream()
                    .map(s -> AntlrSchemaTypeVisitor.unquote(s.getText()))
                    .collect(Collectors.toSet());
            return new AntlrEnumEntityType(namespace, name, Collections.emptySet(), names, location(ctx));
        } else {
            AntlrRecordType shape = new AntlrRecordType(Collections.emptyMap(), SourceLoc.MISSING);
            if (ctx.record() != null) {
                shape = (AntlrRecordType) types.visit(ctx.record());
            }
            Set<AntlrTypeReference> refs = Collections.emptySet();
            if (ctx.paths() != null) {
                refs = ctx.paths().path().stream()
                        .map(p -> (AntlrTypeReference) types.visit(p))
                        .collect(Collectors.toSet());
            }
            return new AntlrRecordEntityType(namespace, name, refs, shape, location(ctx));
        }
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
