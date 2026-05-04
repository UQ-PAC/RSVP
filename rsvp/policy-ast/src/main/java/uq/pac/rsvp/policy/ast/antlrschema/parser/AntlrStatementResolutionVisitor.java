package uq.pac.rsvp.policy.ast.antlrschema.parser;

import uq.pac.rsvp.policy.ast.antlrschema.AntlrSchema;
import uq.pac.rsvp.policy.ast.antlrschema.statement.*;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrBuiltinType;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrRecordType;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrTypeReference;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaValueVisitor;
import uq.pac.rsvp.policy.ast.schema.SchemaResolutionException;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static uq.pac.rsvp.Assertion.require;

/**
 * Resolution for types in the presence of schema
 */
public class AntlrStatementResolutionVisitor implements AntlrSchemaValueVisitor<AntlrSchemaStatement> {

    private final AntlrSchema schema;
    private final AntlrTypesResolutionVisitor types;

    public AntlrStatementResolutionVisitor(AntlrSchema schema, String namespace) {
        this.schema = schema;
        this.types = new AntlrTypesResolutionVisitor(schema, namespace);
    }

    private AntlrTypeReference validateReference(AntlrTypeReference ref,
               Class<? extends AntlrSchemaStatement> expectedCls, String expectedMsg) {

        AntlrTypeReference type = (AntlrTypeReference) types.visitReference(ref);
        AntlrSchemaStatement stmt = schema.get(type);

        // The previous visitor should have ensured resolution
        // that's a bug if not found at this stage
        require(stmt != null);

        // Check context of the statement
        if (!expectedCls.isInstance(stmt)) {
            throw new SchemaResolutionException("Expected %s reference: %s".formatted(expectedMsg, type.toString()));
        }

        return type;
    }

    private AntlrTypeReference validateEntityReference(AntlrTypeReference ref) {
        return validateReference(ref, AntlrEntityType.class, "entity");
    }

    private AntlrTypeReference validateActionReference(AntlrTypeReference ref) {
        return validateReference(ref, AntlrAction.class, "action");
    }

    // The namespace for type resolution is supplied as an argument
    // that is then used to create the type visitor. We need to ensure
    // That a statement is created in the same namespace where type
    // resolution happens
    private void validateNamespace(AntlrSchemaStatement stmt) {
        require(stmt.getReference().getNamespace().equals(types.getNamespace()));
    }

    @Override
    public AntlrSchemaStatement visitRecordEntity(AntlrRecordEntityType entity) {
        validateNamespace(entity);
        AntlrTypeReference ref = validateEntityReference(entity.getReference());
        // Entity reference itself should be fully set and resolved during definition
        require(ref.equals(entity.getReference()));
        Collection<AntlrTypeReference> memberOf = entity.getMemberOf()
                .stream()
                .map(this::validateEntityReference)
                .toList();
        AntlrRecordType shape = (AntlrRecordType) entity.getShape().compute(types);
        return new AntlrRecordEntityType(ref, memberOf, shape, entity.getAnnotations(), entity.getSourceLoc());
    }

    @Override
    public AntlrSchemaStatement visitEnumEntity(AntlrEnumEntityType entity) {
        validateNamespace(entity);
        AntlrTypeReference ref = validateEntityReference(entity.getReference());
        // Entity reference itself should be fully set and resolved during definition
        require(ref.equals(entity.getReference()));
        return entity;
    }

    @Override
    public AntlrSchemaStatement visitAction(AntlrAction action) {
        validateNamespace(action);
        AntlrTypeReference ref = validateActionReference(action.getReference());
        // Entity reference itself should be fully set and resolved during definition
        require(ref.equals(action.getReference()));
        // Check member-of references
        Set<AntlrTypeReference> memberOf = action.getMemberOf()
                .stream()
                .map(this::validateActionReference)
                .collect(Collectors.toSet());

        AntlrActionApplication appliesTo = action.getApplication();
        // Check principal and resource types
        Collection<AntlrTypeReference> principalTypes = appliesTo.getPrincipalTypes()
                .stream()
                .map(this::validateEntityReference)
                .toList();
        Collection<AntlrTypeReference> resourceTypes = appliesTo.getResourceTypes()
                .stream()
                .map(this::validateEntityReference)
                .toList();
        // Check context
        AntlrRecordType context = (AntlrRecordType) appliesTo.getContext().compute(types);
        appliesTo = new AntlrActionApplication(principalTypes, resourceTypes, context);
        // rebuild action
        return new AntlrAction(ref, memberOf, appliesTo, action.getAnnotations(), action.getSourceLoc());
    }

    @Override
    public AntlrSchemaStatement visitCommon(AntlrCommonType type) {
        validateNamespace(type);
        AntlrBuiltinType definition = type.getDefinition().compute(types);
        return new AntlrCommonType(type.getReference(), definition, type.getAnnotations(), type.getSourceLoc());
    }
}
