package uq.pac.rsvp.policy.ast.visitor;

import uq.pac.rsvp.policy.ast.schema.ActionDefinition;
import uq.pac.rsvp.policy.ast.schema.EntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.common.*;

public interface SchemaComputationVisitor<T> {
    public T visitSchema(Schema schema);

    public T visitEntityTypeDefinition(EntityTypeDefinition type);

    public T visitActionDefinition(ActionDefinition action);

    public T visitRecordTypeDefinition(RecordTypeDefinition type);

    public T visitSetTypeDefinition(SetTypeDefinition type);

    // Type references
    public T visitEntityTypeReference(EntityTypeReference type);

    public T visitCommonTypeReference(CommonTypeReference type);

    // Primitive types
    public T visitBoolean(BooleanType type);

    public T visitLong(LongType type);

    public T visitString(StringType type);

    // Extension types
    public T visitDateTime(DateTimeType type);

    public T visitDecimal(DecimalType type);

    public T visitDuration(DurationType type);

    public T visitIpAddress(IpAddressType type);

    // Unresolved
    public T visitUnresolvedTypeReference(UnresolvedTypeReference type);
}
