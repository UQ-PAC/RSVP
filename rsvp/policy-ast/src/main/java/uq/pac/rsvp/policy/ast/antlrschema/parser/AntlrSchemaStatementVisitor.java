package uq.pac.rsvp.policy.ast.antlrschema.parser;

import uq.pac.rsvp.policy.ast.CedarschemaParser;
import uq.pac.rsvp.policy.ast.antlrschema.statement.*;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrBuiltinType;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrRecordType;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrTypeReference;
import uq.pac.rsvp.support.FileSource;
import uq.pac.rsvp.support.SourceLoc;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static uq.pac.rsvp.Assertion.require;
import static uq.pac.rsvp.policy.ast.antlrschema.parser.AntlrSchemaTypeVisitor.unquote;

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
                    .map(s -> unquote(s.getText()))
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

    private String getNornalisedActionName(CedarschemaParser.NameContext ctx) {
        String actionName = ctx.getText();
        if (ctx.STRING() == null) {
            actionName = '"' + actionName + '"';
        }
        return "Action::" + actionName;
    }

    @Override
    public AntlrSchemaStatement visitAction(CedarschemaParser.ActionContext ctx) {
        require(ctx.name().size() == 1); // FIXME

        // Quoted action name in the form Action::"name"
        String actionName = getNornalisedActionName(ctx.name(0));

        // Member of references
        Set<AntlrTypeReference> references = Collections.emptySet();
        if (ctx.actionRefs() != null) {
            references = ctx.actionRefs().actionRef()
                    .stream()
                    .map(ref -> {
                        String name;
                        String namespace;
                        if (ref.name() != null) {
                            namespace = this.namespace;
                            name = getNornalisedActionName(ref.name());
                        } else {
                            namespace = ref.path() == null ? this.namespace : ref.path().getText();
                            name = "Action::" + ref.STRING();
                        }
                        return new AntlrTypeReference(namespace, name, location(ref));
                    }).collect(Collectors.toUnmodifiableSet());
        }

        Set<AntlrTypeReference> principalTypes = Collections.emptySet();
        Set<AntlrTypeReference> resourceTypes = Collections.emptySet();
        AntlrRecordType context = new AntlrRecordType(Collections.emptyMap(), SourceLoc.MISSING);

        CedarschemaParser.AppliesToContext appliesTo = ctx.appliesTo();

        if (appliesTo != null) {
            principalTypes = appliesTo.paths(0).path().stream()
                        .map(p -> (AntlrTypeReference) types.visit(p))
                        .collect(Collectors.toSet());
            resourceTypes = appliesTo.paths(1).path().stream()
                    .map(p -> (AntlrTypeReference) types.visit(p))
                    .collect(Collectors.toSet());

            if (appliesTo.record() != null) {
                context = (AntlrRecordType) types.visit(appliesTo.record());
            }
        }
        AntlrActionApplication appliesToNode =
                new AntlrActionApplication(principalTypes, resourceTypes, context);

        return new AntlrAction(namespace, actionName, references, appliesToNode, location(ctx));
    }
}
