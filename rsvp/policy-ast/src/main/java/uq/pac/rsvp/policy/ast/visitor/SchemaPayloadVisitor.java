package uq.pac.rsvp.policy.ast.visitor;

import uq.pac.rsvp.policy.ast.schema.ActionDefinition;
import uq.pac.rsvp.policy.ast.schema.EntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.common.*;

public interface SchemaPayloadVisitor<T> {
    void visitSchema(Schema schema, T payload);

    void visitEntityTypeDefinition(EntityTypeDefinition type, T payload);

    void visitActionDefinition(ActionDefinition action, T payload);

    void visitRecordTypeDefinition(RecordTypeDefinition type, T payload);

    void visitSetTypeDefinition(SetTypeDefinition type, T payload);

    // Type references
    void visitEntityTypeReference(EntityTypeReference type, T payload);

    void visitCommonTypeReference(CommonTypeReference type, T payload);

    // Primitive types
    void visitBoolean(BooleanType type, T payload);

    void visitLong(LongType type, T payload);

    void visitString(StringType type, T payload);

    // Extension types
    void visitDateTime(DateTimeType type, T payload);

    void visitDecimal(DecimalType type, T payload);

    void visitDuration(DurationType type, T payload);

    void visitIpAddress(IpAddressType type, T payload);

    // Undefined
    void visitUnresolvedTypeReference(UnresolvedTypeReference type, T payload);
}
