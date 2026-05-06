package uq.pac.rsvp.policy.ast.schema.visitor;

import uq.pac.rsvp.policy.ast.schema.*;
import uq.pac.rsvp.policy.ast.schema.statement.Action;
import uq.pac.rsvp.policy.ast.schema.statement.CommonType;
import uq.pac.rsvp.policy.ast.schema.statement.EnumEntityType;
import uq.pac.rsvp.policy.ast.schema.statement.RecordEntityType;
import uq.pac.rsvp.policy.ast.schema.type.*;

public interface SchemaVisitor {
    void visitSchema(Schema schema);

    // Statements
    void visitRecordEntity(RecordEntityType entity);

    void visitEnumEntity(EnumEntityType entity);

    void visitAction(Action action);

    void visitCommon(CommonType type);

    // Compound types
    void visitRecord(RecordType type);

    void visitSet(SetType type);

    // Type references
    void visitTypeReference(TypeReference type);

    // Primitive types
    void visitBoolean(BooleanType type);

    void visitLong(LongType type);

    void visitString(StringType type);

    // Extension types
    void visitIpAddress(IpAddressType type);

    void visitDecimal(DecimalType type);

    void visitDateTime(DateTimeType type);

    void visitDuration(DurationType type);
}
