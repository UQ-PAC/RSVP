package uq.pac.rsvp.policy.ast.schema;

import org.antlr.v4.runtime.RuleContext;
import uq.pac.rsvp.policy.ast.CedarschemaParser;
import uq.pac.rsvp.policy.ast.schema.common.RecordTypeDefinition;
import uq.pac.rsvp.support.FileSource;

import java.util.*;
import java.util.stream.Collectors;


class SchemaStatementVisitor extends SourceVisitor<SchemaStatement> {

    private final SchemaTypeVisitor types;
    private final Namespace namespace;

    public SchemaStatementVisitor(FileSource fs, SchemaTypeVisitor types, Namespace ns) {
        super(fs);
        this.types = types;
        this.namespace = ns;
    }

    @Override
    public SchemaStatement visitEntity(CedarschemaParser.EntityContext ctx) {
        // Multiple
        String name = ctx.entityNames().ID().getFirst().getText();
        if (!namespace.getName().isEmpty()) {
            name = namespace.getName() + "::" + name;
        }

        Set<String> unresolvedMemberOf = new HashSet<>();
        if (ctx.paths() != null) {
            unresolvedMemberOf = ctx.paths().path().stream()
                    .map(RuleContext::getText)
                    .collect(Collectors.toSet());
        }

        RecordTypeDefinition rec = new RecordTypeDefinition();
        if (ctx.record() != null) {
            rec = (RecordTypeDefinition) types.visit(ctx.record());
        }

        Set<String> enumMembers = new HashSet<>();
        if (ctx.strings() != null) {
            enumMembers = ctx.strings().STRING().stream()
                    .map(s -> SchemaTypeVisitor.unquote(s.getText()))
                    .collect(Collectors.toSet());
        }

        return new EntityTypeDefinition(name, unresolvedMemberOf, rec.getAttributes(), enumMembers);
    }

    @Override
    public SchemaStatement visitCommon(CedarschemaParser.CommonContext ctx) {
        return types.visit(ctx);
    }

    @Override
    public SchemaStatement visitAction(CedarschemaParser.ActionContext ctx) {
        // FIXME: Multiple
        CedarschemaParser.NameContext nameCtx = ctx.name().getFirst();
        String eid = SchemaTypeVisitor.unquote(nameCtx.getText());
        String type = namespace.getName().isBlank() ? "Action" : namespace.getName() + "::Action";
        Map<String, String> annotations = Collections.emptyMap();
        Set<String> resourceTypes = new HashSet<>();
        Set<String> principalTypes = new HashSet<>();
        RecordTypeDefinition context = new RecordTypeDefinition();
        if (ctx.appliesTo() != null) {
            ctx.appliesTo().paths(0).path().forEach(p -> principalTypes.add(p.getText()));
            ctx.appliesTo().paths(1).path().forEach(p -> resourceTypes.add(p.getText()));

            if (ctx.appliesTo().record() != null) {
                context = (RecordTypeDefinition) types.visit(ctx.appliesTo().record());
            }
        }

        Set<ActionDefinition.ActionReference> unresolvedMemberOf = new HashSet<>();
        if (ctx.actionRefs() != null) {
            ctx.actionRefs().actionRef().forEach(m -> {
                String tp = (m.path() == null) ? m.path().getText() : "";
                String id = SchemaTypeVisitor.unquote(m.name().getText());
                ActionDefinition.ActionReference ref = new ActionDefinition.ActionReference(id, tp);
                unresolvedMemberOf.add(ref);
            });
        }
        return new ActionDefinition(type, eid, unresolvedMemberOf, principalTypes, resourceTypes, context);
    }
}
