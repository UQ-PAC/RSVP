package uq.pac.rsvp.policy.ast.visitor;

import uq.pac.rsvp.policy.ast.schema.ActionDefinition;
import uq.pac.rsvp.policy.ast.schema.EntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.common.*;

public interface SchemaVisitor {
    void visitSchema(Schema schema);

    void visitEntityTypeDefinition(EntityTypeDefinition type);

    void visitActionDefinition(ActionDefinition action);

    void visitRecordTypeDefinition(RecordTypeDefinition type);

    void visitSetTypeDefinition(SetTypeDefinition type);

    // Type references
    void visitEntityTypeReference(EntityTypeReference type);

    void visitCommonTypeReference(CommonTypeReference type);

    // Primitive types
    void visitBoolean(BooleanType type);

    void visitLong(LongType type);

    void visitString(StringType type);

    // Extension types
    void visitDateTime(DateTimeType type);

    void visitDecimal(DecimalType type);

    void visitDuration(DurationType type);

    void visitIpAddress(IpAddressType type);

    // Unresolved
    void visitUnresolvedTypeReference(UnresolvedTypeReference type);
}
