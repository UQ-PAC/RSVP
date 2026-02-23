package uq.pac.rsvp.policy.ast.visitor;

import java.util.Map;

import uq.pac.rsvp.policy.ast.schema.ActionDefinition;
import uq.pac.rsvp.policy.ast.schema.EntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.Namespace;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.common.RecordTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.common.SetTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;

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
            EntityTypeDefinition entityType = namespace.getEntityType(name);
            entityType.setName(name);
            entityType.accept(this);
        }

        for (String name : namespace.actionNames()) {
            ActionDefinition action = namespace.getAction(name);
            action.setName(name);
            action.accept(this);
        }

        for (String name : namespace.commonTypeNames()) {
            CommonTypeDefinition commonType = namespace.getCommonType(name);

            if (!commonType.isResolved()) {
                commonType = Schema.resolveTypeReference(commonType.getName(), schema, namespace);

                if (commonType == null) {
                    continue; // TODO: error reporting
                }

                namespace.resolveCommonType(name, commonType);
            }

            commonType.setName(name);
            commonType.accept(this);
        }
    }

    @Override
    public void visitEntityTypeDefinition(EntityTypeDefinition type) {
        type.resolveMemberOfTypes(schema, namespace);

        for (String name : type.getShapeAttributeNames()) {
            CommonTypeDefinition attribute = type.getShapeAttributeType(name);

            if (!attribute.isResolved()) {
                attribute = Schema.resolveTypeReference(attribute.getName(), schema, namespace);

                if (attribute == null) {
                    continue; // TODO: error reporting
                }

                type.resolveShapeAttributeType(name, attribute);
            }

            attribute.setName(name);
            attribute.accept(this);
        }

    }

    @Override
    public void visitActionDefinition(ActionDefinition action) {
        action.resolveReferences(schema, namespace);
        if (action.getAppliesToContext() != null) {
            action.getAppliesToContext().accept(this);
        }
    }

    @Override
    public void visitRecordTypeDefinition(RecordTypeDefinition type) {
        for (String name : type.getAttributeNames()) {
            CommonTypeDefinition prop = type.getAttributeType(name);

            if (!prop.isResolved()) {
                prop = Schema.resolveTypeReference(prop.getName(), schema, namespace);

                if (prop == null) {
                    continue; // TODO: error reporting
                }

                type.resolveAttributeType(name, prop);
            }

            prop.setName(name);
            prop.accept(this);
        }
    }

    @Override
    public void visitSetTypeDefinition(SetTypeDefinition type) {
        CommonTypeDefinition element = type.getElementType();

        if (!element.isResolved()) {
            element = Schema.resolveTypeReference(element.getName(), schema, namespace);

            if (element == null) {
                return; // TODO: error reporting
            }

            type.resolveElementType(element);

        }

        element.accept(this);
    }

}
