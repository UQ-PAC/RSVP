package uq.pac.rsvp.policy.ast.visitor;

import java.util.Map;

import uq.pac.rsvp.policy.ast.schema.Action;
import uq.pac.rsvp.policy.ast.schema.EntityType;
import uq.pac.rsvp.policy.ast.schema.Namespace;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.attribute.AttributeType;
import uq.pac.rsvp.policy.ast.schema.attribute.EntityOrCommonType;
import uq.pac.rsvp.policy.ast.schema.attribute.RecordType;

// Set definition names and resolve all references to entity types
public class SchemaResolutionVisitor extends SchemaVisitorImpl {

    private Schema schema;
    private Namespace namespace;

    @Override
    public void visitSchema(Schema schema) {
        this.schema = schema;
        for (Map.Entry<String, Namespace> entry : schema.entrySet()) {
            this.namespace = entry.getValue();
            entry.getValue().setName(entry.getKey());
            entry.getValue().accept(this);
        }
    }

    @Override
    public void visitNamespace(Namespace namespace) {
        for (String name : namespace.entityTypeNames()) {
            EntityType entityType = namespace.getEntityType(name);
            entityType.setName(name);
            entityType.accept(this);
        }

        for (String name : namespace.actionNames()) {
            Action action = namespace.getAction(name);
            action.setName(name);
            action.accept(this);
        }

        for (String name : namespace.commonTypeNames()) {
            AttributeType commonType = namespace.getCommonType(name);
            commonType.setDefinitionName(name);
            commonType.accept(this);
        }
    }

    @Override
    public void visitEntityType(EntityType type) {
        type.resolveMemberOfTypes(schema, namespace);

        for (String name : type.getShapeAttributeNames()) {
            AttributeType attribute = type.getShapeAttributeType(name);
            attribute.setDefinitionName(name);
            attribute.accept(this);
        }

    }

    @Override
    public void visitAction(Action action) {
        action.resolveReferences(schema, namespace);
        if (action.getAppliesToContext() != null) {
            action.getAppliesToContext().accept(this);
        }
    }

    @Override
    public void visitEntityOrCommonAttributeType(EntityOrCommonType type) {
        type.resolve(schema, namespace);
    }

    @Override
    public void visitRecordAttributeType(RecordType type) {
        for (String name : type.getAttributeNames()) {
            AttributeType prop = type.getAttributeType(name);
            prop.setDefinitionName(name);
            prop.accept(this);
        }
    }

}
