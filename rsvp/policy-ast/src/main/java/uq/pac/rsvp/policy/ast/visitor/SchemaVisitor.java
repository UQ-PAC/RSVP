package uq.pac.rsvp.policy.ast.visitor;

import uq.pac.rsvp.policy.ast.schema.ActionDefinition;
import uq.pac.rsvp.policy.ast.schema.EntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.common.BooleanType;
import uq.pac.rsvp.policy.ast.schema.common.CommonTypeReference;
import uq.pac.rsvp.policy.ast.schema.common.DateTimeType;
import uq.pac.rsvp.policy.ast.schema.common.DecimalType;
import uq.pac.rsvp.policy.ast.schema.common.DurationType;
import uq.pac.rsvp.policy.ast.schema.common.EntityTypeReference;
import uq.pac.rsvp.policy.ast.schema.common.IpAddressType;
import uq.pac.rsvp.policy.ast.schema.common.LongType;
import uq.pac.rsvp.policy.ast.schema.common.RecordTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.common.SetTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.common.StringType;

public interface SchemaVisitor {
    public void visitSchema(Schema schema);

    public void visitEntityTypeDefinition(EntityTypeDefinition type);

    public void visitActionDefinition(ActionDefinition action);

    public void visitRecordTypeDefinition(RecordTypeDefinition type);

    public void visitSetTypeDefinition(SetTypeDefinition type);

    // Type references
    public void visitEntityTypeReference(EntityTypeReference type);

    public void visitCommonTypeReference(CommonTypeReference type);

    // Primitive types
    public void visitBoolean(BooleanType type);

    public void visitLong(LongType type);

    public void visitString(StringType type);

    // Extension types
    public void visitDateTime(DateTimeType type);

    public void visitDecimal(DecimalType type);

    public void visitDuration(DurationType type);

    public void visitIpAddress(IpAddressType type);

}
