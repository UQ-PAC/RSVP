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

            for (String name : this.namespace.entityTypeNames()) {
                EntityTypeDefinition entityType = this.namespace.getEntityType(name);
                schema.addEntityType(entityType);
                entityType.accept(this);
            }

            for (String name : this.namespace.actionNames()) {
                ActionDefinition action = this.namespace.getAction(name);
                schema.addAction(action);
                action.accept(this);
            }

            for (String name : this.namespace.commonTypeNames()) {
                CommonTypeDefinition commonType = this.namespace.getCommonType(name);

                if (commonType instanceof UnresolvedTypeReference) {
                    commonType = Schema.resolveTypeReference((UnresolvedTypeReference) commonType, schema,
                            this.namespace);
                    this.namespace.resolveCommonType(name, commonType);
                }

                schema.addCommonType(commonType);
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
                attribute = Schema.resolveTypeReference((UnresolvedTypeReference) attribute, schema, namespace);
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
                prop = Schema.resolveTypeReference((UnresolvedTypeReference) prop, schema, namespace);
                type.resolveAttributeType(name, prop);
            }

            prop.accept(this);
        }
    }

    @Override
    public void visitSetTypeDefinition(SetTypeDefinition type) {
        CommonTypeDefinition element = type.getElementType();

        if (element instanceof UnresolvedTypeReference) {
            element = Schema.resolveTypeReference((UnresolvedTypeReference) element, schema, namespace);
            type.resolveElementType(element);

        }

        element.accept(this);
    }

}
