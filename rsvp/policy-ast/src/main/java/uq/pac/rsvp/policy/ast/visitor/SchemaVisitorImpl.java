package uq.pac.rsvp.policy.ast.visitor;

import uq.pac.rsvp.policy.ast.schema.Action;
import uq.pac.rsvp.policy.ast.schema.EntityType;
import uq.pac.rsvp.policy.ast.schema.Namespace;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.attribute.EntityOrCommonType;
import uq.pac.rsvp.policy.ast.schema.attribute.ExtensionType;
import uq.pac.rsvp.policy.ast.schema.attribute.PrimitiveType;
import uq.pac.rsvp.policy.ast.schema.attribute.RecordType;
import uq.pac.rsvp.policy.ast.schema.attribute.SetType;

public class SchemaVisitorImpl implements SchemaVisitor {

    @Override
    public void visitSchema(Schema schema) {
        for (Namespace namespace : schema.values()) {
            namespace.accept(this);
        }
    }

    @Override
    public void visitNamespace(Namespace namespace) {
        for (String entityType : namespace.entityTypeNames()) {
            namespace.getEntityType(entityType).accept(this);
        }

        for (String action : namespace.actionNames()) {
            namespace.getAction(action).accept(this);
        }

        for (String commonType : namespace.commonTypeNames()) {
            namespace.getCommonType(commonType).accept(this);
        }
    }

    @Override
    public void visitEntityType(EntityType type) {
        for (String attributeType : type.getShapeAttributeNames()) {
            type.getShapeAttributeType(attributeType).accept(this);
        }
    }

    @Override
    public void visitAction(Action action) {
        if (action.getAppliesToContext() != null) {
            action.getAppliesToContext().accept(this);
        }
    }

    @Override
    public void visitEntityOrCommonAttributeType(EntityOrCommonType type) {
    }

    @Override
    public void visitExtensionAttributeType(ExtensionType type) {
    }

    @Override
    public void visitPrimitiveAttributeType(PrimitiveType type) {
    }

    @Override
    public void visitRecordAttributeType(RecordType type) {
        for (String attribute : type.getAttributeNames()) {
            type.getAttributeType(attribute).accept(this);
        }
    }

    @Override
    public void visitSetAttributeType(SetType type) {
        type.getElementType().accept(this);
    }

}
