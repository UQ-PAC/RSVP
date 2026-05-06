package uq.pac.rsvp.policy.ast.schema.parser;

import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.statement.*;
import uq.pac.rsvp.policy.ast.schema.type.BuiltinType;
import uq.pac.rsvp.policy.ast.schema.type.RecordType;
import uq.pac.rsvp.policy.ast.schema.type.TypeReference;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.schema.SchemaResolutionException;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static uq.pac.rsvp.Assertion.require;

/**
 * Resolution for types in the presence of schema
 */
public class StatementResolutionVisitor implements SchemaComputationVisitor<SchemaStatement> {

    private final Schema schema;
    private final TypesResolutionVisitor types;

    public StatementResolutionVisitor(Schema schema, String namespace) {
        this.schema = schema;
        this.types = new TypesResolutionVisitor(schema, namespace);
    }

    private TypeReference validateReference(TypeReference ref,
                                            Class<? extends SchemaStatement> expectedCls, String expectedMsg) {

        TypeReference type = (TypeReference) types.visitTypeReference(ref);
        SchemaStatement stmt = schema.get(type);

        // The previous visitor should have ensured resolution
        // that's a bug if not found at this stage
        require(stmt != null);

        // Check context of the statement
        if (!expectedCls.isInstance(stmt)) {
            throw new SchemaResolutionException("Expected %s reference: %s".formatted(expectedMsg, type.toString()));
        }

        return type;
    }

    private TypeReference validateEntityReference(TypeReference ref) {
        return validateReference(ref, EntityTypeDefinition.class, "entity");
    }

    private TypeReference validateActionReference(TypeReference ref) {
        return validateReference(ref, ActionDefinition.class, "action");
    }

    // The namespace for type resolution is supplied as an argument
    // that is then used to create the type visitor. We need to ensure
    // That a statement is created in the same namespace where type
    // resolution happens
    private void validateNamespace(SchemaStatement stmt) {
        require(stmt.getTypeReference().getNamespace().equals(types.getNamespace()));
    }

    @Override
    public SchemaStatement visitRecordEntity(RecordEntityTypeDefinition entity) {
        validateNamespace(entity);
        TypeReference ref = validateEntityReference(entity.getTypeReference());
        // Entity reference itself should be fully set and resolved during definition
        require(ref.equals(entity.getTypeReference()));
        Collection<TypeReference> memberOf = entity.getMemberOf()
                .stream()
                .map(this::validateEntityReference)
                .toList();
        RecordType shape = (RecordType) entity.getShape().compute(types);
        return new RecordEntityTypeDefinition(ref, memberOf, shape, entity.getAnnotations(), entity.getSourceLoc());
    }

    @Override
    public SchemaStatement visitEnumEntity(EnumEntityTypeDefinition entity) {
        validateNamespace(entity);
        TypeReference ref = validateEntityReference(entity.getTypeReference());
        // Entity reference itself should be fully set and resolved during definition
        require(ref.equals(entity.getTypeReference()));
        return entity;
    }

    @Override
    public SchemaStatement visitAction(ActionDefinition action) {
        validateNamespace(action);
        TypeReference ref = validateActionReference(action.getTypeReference());
        // Entity reference itself should be fully set and resolved during definition
        require(ref.equals(action.getTypeReference()));
        // Check member-of references
        Set<TypeReference> memberOf = action.getMemberOf()
                .stream()
                .map(this::validateActionReference)
                .collect(Collectors.toSet());

        ActionApplication appliesTo = action.getApplication();
        // Check principal and resource types
        Collection<TypeReference> principalTypes = appliesTo.getPrincipalTypes()
                .stream()
                .map(this::validateEntityReference)
                .toList();
        Collection<TypeReference> resourceTypes = appliesTo.getResourceTypes()
                .stream()
                .map(this::validateEntityReference)
                .toList();
        // Check context
        RecordType context = (RecordType) appliesTo.getContext().compute(types);
        appliesTo = new ActionApplication(principalTypes, resourceTypes, context);
        // rebuild action
        return new ActionDefinition(ref, memberOf, appliesTo, action.getAnnotations(), action.getSourceLoc());
    }

    @Override
    public SchemaStatement visitCommon(CommonTypeDefinition type) {
        validateNamespace(type);
        BuiltinType definition = type.getDefinition().compute(types);
        return new CommonTypeDefinition(type.getTypeReference(), definition, type.getAnnotations(), type.getSourceLoc());
    }
}
