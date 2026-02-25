package uq.pac.rsvp.policy.ast.visitor;

import java.util.Map;

import uq.pac.rsvp.policy.ast.schema.ActionDefinition;
import uq.pac.rsvp.policy.ast.schema.EntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.Namespace;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.common.RecordTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.common.SetTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.common.UnresolvedTypeReference;
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

            String namespaceName = entry.getKey();

            this.namespace.setName(namespaceName);

            boolean topNamespace = namespaceName.isEmpty();

            for (String name : this.namespace.entityTypeNames()) {
                EntityTypeDefinition entityType = this.namespace.getEntityType(name);
                String qualifiedName = topNamespace ? name : namespaceName + "::" + name;
                schema.putEntityType(qualifiedName, entityType);
                entityType.setName(qualifiedName);
                entityType.accept(this);
            }

            for (String name : this.namespace.actionNames()) {
                ActionDefinition action = this.namespace.getAction(name);
                String qualifiedName = topNamespace ? "Action::" + name : namespaceName + "::Action::" + name;
                schema.putAction(qualifiedName, action);
                action.setName(qualifiedName);
                action.accept(this);
            }

            for (String name : this.namespace.commonTypeNames()) {
                CommonTypeDefinition commonType = this.namespace.getCommonType(name);

                if (commonType instanceof UnresolvedTypeReference) {
                    String typeName = ((UnresolvedTypeReference) commonType).getRawTypeName();
                    commonType = Schema.resolveTypeReference(typeName, schema, this.namespace);

                    if (commonType == null) {
                        continue; // TODO: error reporting
                    }

                    this.namespace.resolveCommonType(name, commonType);
                }

                String qualifiedName = topNamespace ? name : namespaceName + "::" + name;
                schema.putCommonType(qualifiedName, commonType);
                commonType.setName(qualifiedName);
                commonType.accept(this);
            }
        }
    }

    @Override
    public void visitEntityTypeDefinition(EntityTypeDefinition type) {
        type.resolveMemberOfTypes(schema, namespace);

        for (String name : type.getShapeAttributeNames()) {
            CommonTypeDefinition attribute = type.getShapeAttributeType(name);

            if (attribute instanceof UnresolvedTypeReference) {
                String typeName = ((UnresolvedTypeReference) attribute).getRawTypeName();
                attribute = Schema.resolveTypeReference(typeName, schema, namespace);

                if (attribute == null) {
                    continue; // TODO: error reporting
                }

                type.resolveShapeAttributeType(name, attribute);
            }

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

            if (prop instanceof UnresolvedTypeReference) {
                String typeName = ((UnresolvedTypeReference) prop).getRawTypeName();
                prop = Schema.resolveTypeReference(typeName, schema, namespace);

                if (prop == null) {
                    continue; // TODO: error reporting
                }

                type.resolveAttributeType(name, prop);
            }

            prop.accept(this);
        }
    }

    @Override
    public void visitSetTypeDefinition(SetTypeDefinition type) {
        CommonTypeDefinition element = type.getElementType();

        if (element instanceof UnresolvedTypeReference) {
            String typeName = ((UnresolvedTypeReference) element).getRawTypeName();
            element = Schema.resolveTypeReference(typeName, schema, namespace);

            if (element == null) {
                return; // TODO: error reporting
            }

            type.resolveElementType(element);

        }

        element.accept(this);
    }

}
