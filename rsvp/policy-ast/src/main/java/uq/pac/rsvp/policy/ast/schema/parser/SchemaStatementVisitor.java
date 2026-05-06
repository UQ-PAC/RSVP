package uq.pac.rsvp.policy.ast.schema.parser;

import uq.pac.rsvp.policy.ast.CedarschemaParser;
import uq.pac.rsvp.policy.ast.schema.statement.*;
import uq.pac.rsvp.policy.ast.schema.type.BuiltinType;
import uq.pac.rsvp.policy.ast.schema.type.RecordType;
import uq.pac.rsvp.policy.ast.schema.type.TypeReference;
import uq.pac.rsvp.support.FileSource;
import uq.pac.rsvp.support.SourceLoc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static uq.pac.rsvp.policy.ast.schema.parser.SchemaTypeVisitor.unquote;

/**
 * A top-level visitor generating schema statements from the parser
 */
class SchemaStatementVisitor extends CedarschemaSourceVisitor<List<SchemaStatement>> {

    private final SchemaTypeVisitor types;
    private final String namespace;

    public SchemaStatementVisitor(FileSource fs, String namespace) {
        super(fs);
        this.namespace = namespace;
        this.types = new SchemaTypeVisitor(fs, namespace);
    }

    private static Annotations getAnnotations(List<CedarschemaParser.AnnotationContext> ctx) {
        Annotations.Builder builder = new Annotations.Builder();
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
    public List<SchemaStatement> visitEntity(CedarschemaParser.EntityContext ctx) {
        return ctx.entityNames().ID().stream().map(id -> {
            TypeReference ref = new TypeReference(namespace, id.getText(), location(id.getSymbol()));
            Annotations annotations = getAnnotations(ctx.annotation());

            if (ctx.strings() != null) {
                Collection<String> names = ctx.strings().STRING().stream()
                        .map(s -> unquote(s.getText()))
                        .toList();
                return new EnumEntityTypeDefinition(ref, Collections.emptySet(), names, annotations, location(ctx));
            } else {
                RecordType shape = new RecordType(Collections.emptyMap(), SourceLoc.MISSING);
                if (ctx.record() != null) {
                    shape = (RecordType) types.visit(ctx.record());
                }
                Set<TypeReference> refs = Collections.emptySet();
                if (ctx.paths() != null) {
                    refs = ctx.paths().path().stream()
                            .map(p -> (TypeReference) types.visit(p))
                            .collect(Collectors.toSet());
                }
                return (SchemaStatement) new RecordEntityTypeDefinition(ref, refs, shape, annotations, location(ctx));
            }
        }).toList();
    }

    @Override
    public List<SchemaStatement> visitCommon(CedarschemaParser.CommonContext ctx) {
        BuiltinType definition = types.visit(ctx.type());
        Annotations annotations = getAnnotations(ctx.annotation());
        TypeReference reference = new TypeReference(namespace, ctx.typename().getText(), location(ctx.typename()));
        return List.of(new CommonTypeDefinition(reference, definition, annotations, location(ctx)));
    }

    private String getNormalisedActionName(CedarschemaParser.NameContext ctx) {
        String actionName = ctx.getText();
        if (ctx.STRING() == null) {
            actionName = '"' + actionName + '"';
        }
        return "Action::" + actionName;
    }

    @Override
    public List<SchemaStatement> visitAction(CedarschemaParser.ActionContext ctx) {
        return ctx.name().stream().map(nameCtx -> {
            // Quoted action name in the form Action::"name"
            String actionName = getNormalisedActionName(nameCtx);
            TypeReference actionReference = new TypeReference(namespace, actionName, location(ctx.name(0)));
            Annotations annotations = getAnnotations(ctx.annotation());

            // Member of references
            Set<TypeReference> references = Collections.emptySet();
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
                            return new TypeReference(namespace, name, location(ref));
                        }).collect(Collectors.toUnmodifiableSet());
            }

            Collection<TypeReference> principalTypes = Collections.emptySet();
            Collection<TypeReference> resourceTypes = Collections.emptySet();
            RecordType context = new RecordType(Collections.emptyMap(), SourceLoc.MISSING);

            CedarschemaParser.AppliesToContext appliesTo = ctx.appliesTo();

            if (appliesTo != null) {
                principalTypes = appliesTo.paths(0).path().stream()
                        .map(p -> (TypeReference) types.visit(p))
                        .toList();
                resourceTypes = appliesTo.paths(1).path().stream()
                        .map(p -> (TypeReference) types.visit(p))
                        .toList();

                if (appliesTo.record() != null) {
                    context = (RecordType) types.visit(appliesTo.record());
                }
            }
            ActionApplication appliesToNode =
                    new ActionApplication(principalTypes, resourceTypes, context);
            return (SchemaStatement) new ActionDefinition(actionReference, references,
                    appliesToNode, annotations, location(ctx));
        }).toList();
    }
}
