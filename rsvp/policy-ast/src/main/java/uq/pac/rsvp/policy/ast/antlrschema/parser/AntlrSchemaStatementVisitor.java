package uq.pac.rsvp.policy.ast.antlrschema.parser;

import uq.pac.rsvp.policy.ast.CedarschemaParser;
import uq.pac.rsvp.policy.ast.antlrschema.statement.*;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrBuiltinType;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrRecordType;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrTypeReference;
import uq.pac.rsvp.support.FileSource;
import uq.pac.rsvp.support.SourceLoc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static uq.pac.rsvp.Assertion.require;
import static uq.pac.rsvp.policy.ast.antlrschema.parser.AntlrSchemaTypeVisitor.unquote;

/**
 * A top-level visitor generating schema statements from the parser
 */
class AntlrSchemaStatementVisitor extends AntlrSourceVisitor<AntlrSchemaStatement> {

    private final AntlrSchemaTypeVisitor types;
    private final String namespace;

    public AntlrSchemaStatementVisitor(FileSource fs, String namespace) {
        super(fs);
        this.namespace = namespace;
        this.types = new AntlrSchemaTypeVisitor(fs, namespace);
    }

    private static AntlrAnnotations getAnnotations(List<CedarschemaParser.AnnotationContext> ctx) {
        AntlrAnnotations.Builder builder = new AntlrAnnotations.Builder();
        if (ctx != null) {
            ctx.forEach(a -> {
                String value = a.STRING() == null ? "" : unquote(a.STRING().getText());
                String key = a.ident().getText();
                builder.add(key, value);
            });
        }
        return builder.build();
    }

    @Override
    public AntlrSchemaStatement visitEntity(CedarschemaParser.EntityContext ctx) {
        require(ctx.entityNames().ID().size() == 1);
        String name = ctx.entityNames().ID(0).getText();
        AntlrTypeReference ref = new AntlrTypeReference(namespace, name);

        AntlrAnnotations annotations = getAnnotations(ctx.annotation());

        if (ctx.strings() != null) {
            Collection<String> names = ctx.strings().STRING().stream()
                    .map(s -> unquote(s.getText()))
                    .toList();
            return new AntlrEnumEntityType(ref, Collections.emptySet(), names, annotations, location(ctx));
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
            return new AntlrRecordEntityType(ref, refs, shape, annotations, location(ctx));
        }
    }

    @Override
    public AntlrSchemaStatement visitCommon(CedarschemaParser.CommonContext ctx) {
        AntlrBuiltinType definition = types.visit(ctx.type());
        AntlrAnnotations annotations = getAnnotations(ctx.annotation());
        AntlrTypeReference reference = new AntlrTypeReference(namespace, ctx.typename().getText());
        return new AntlrCommonType(reference, definition, annotations, location(ctx));
    }

    private String getNormalisedActionName(CedarschemaParser.NameContext ctx) {
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
        String actionName = getNormalisedActionName(ctx.name(0));
        AntlrTypeReference actionReference = new AntlrTypeReference(namespace, actionName);
        AntlrAnnotations annotations = getAnnotations(ctx.annotation());

        // Member of references
        Set<AntlrTypeReference> references = Collections.emptySet();
        if (ctx.actionRefs() != null) {
            references = ctx.actionRefs().actionRef()
                    .stream()
                    .map(ref -> {
                        String name;
                        String namespace;
                        if (ref.name() != null) {
                            namespace = null;
                            name = getNormalisedActionName(ref.name());
                        } else {
                            namespace = ref.path() == null ? null : ref.path().getText();
                            name = "Action::" + ref.STRING();
                        }
                        return new AntlrTypeReference(namespace, name, location(ref));
                    }).collect(Collectors.toUnmodifiableSet());
        }

        Collection<AntlrTypeReference> principalTypes = Collections.emptySet();
        Collection<AntlrTypeReference> resourceTypes = Collections.emptySet();
        AntlrRecordType context = new AntlrRecordType(Collections.emptyMap(), SourceLoc.MISSING);

        CedarschemaParser.AppliesToContext appliesTo = ctx.appliesTo();

        if (appliesTo != null) {
            principalTypes = appliesTo.paths(0).path().stream()
                    .map(p -> (AntlrTypeReference) types.visit(p))
                    .toList();
            resourceTypes = appliesTo.paths(1).path().stream()
                    .map(p -> (AntlrTypeReference) types.visit(p))
                    .toList();

            if (appliesTo.record() != null) {
                context = (AntlrRecordType) types.visit(appliesTo.record());
            }
        }
        AntlrActionApplication appliesToNode =
                new AntlrActionApplication(principalTypes, resourceTypes, context);
        return new AntlrAction(actionReference, references, appliesToNode, annotations, location(ctx));
    }
}
