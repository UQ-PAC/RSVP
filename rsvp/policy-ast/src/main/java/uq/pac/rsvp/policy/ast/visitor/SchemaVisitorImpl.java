package uq.pac.rsvp.policy.ast.visitor;

import uq.pac.rsvp.policy.ast.schema.ActionDefinition;
import uq.pac.rsvp.policy.ast.schema.EntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.common.*;

public class SchemaVisitorImpl implements SchemaVisitor {

    @Override
    public void visitSchema(Schema schema) {
        for (String entityType : schema.entityTypeNames()) {
            schema.getEntityType(entityType).accept(this);
        }

        for (String type : schema.actionTypes()) {
            for (String action : schema.actionNames(type)) {
                schema.getAction(type, action).accept(this);
            }
        }

        for (String commonType : schema.commonTypeNames()) {
            schema.getCommonType(commonType).accept(this);
        }
    }

    @Override
    public void visitEntityTypeDefinition(EntityTypeDefinition type) {
        for (String attributeType : type.getShapeAttributeNames()) {
            type.getShapeAttributeType(attributeType).accept(this);
        }
    }

    @Override
    public void visitActionDefinition(ActionDefinition action) {
        if (action.getAppliesToContext() != null) {
            action.getAppliesToContext().accept(this);
        }
    }

    @Override
    public void visitRecordTypeDefinition(RecordTypeDefinition type) {
        for (String attribute : type.getAttributeNames()) {
            type.getAttributeType(attribute).accept(this);
        }
    }

    @Override
    public void visitSetTypeDefinition(SetTypeDefinition type) {
        type.getElementType().accept(this);
    }

    @Override
    public void visitEntityTypeReference(EntityTypeReference type) {
    }

    @Override
    public void visitCommonTypeReference(CommonTypeReference type) {
    }

    @Override
    public void visitBoolean(BooleanType type) {
    }

    @Override
    public void visitLong(LongType type) {
    }

    @Override
    public void visitString(StringType type) {
    }

    @Override
    public void visitDateTime(DateTimeType type) {
    }

    @Override
    public void visitDecimal(DecimalType type) {
    }

    @Override
    public void visitDuration(DurationType type) {
    }

    @Override
    public void visitIpAddress(IpAddressType type) {
    }

    @Override
    public void visitUnresolvedTypeReference(UnresolvedTypeReference type) {
    }
}
