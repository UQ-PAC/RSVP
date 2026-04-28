package uq.pac.rsvp.policy.ast.antlrschema.visitor;

import uq.pac.rsvp.policy.ast.antlrschema.AntlrSchema;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrAction;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrCommonType;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrEnumEntityType;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrRecordEntityType;
import uq.pac.rsvp.policy.ast.antlrschema.type.*;

public interface AntlrSchemaPayloadVisitor<T> {
    void visitSchema(AntlrSchema schema, T payload);

    // Statements
    void visitRecordEntity(AntlrRecordEntityType entity, T payload);

    void visitEnumEntity(AntlrEnumEntityType entity, T payload);

    void visitAction(AntlrAction action, T payload);

    void visitCommon(AntlrCommonType type, T payload);

    // Compound types
    void visitRecord(AntlrRecordType type, T payload);

    void visitSet(AntlrSetType type, T payload);

    // Type references
    void visitReference(AntlrTypeReference type, T payload);

    // Primitive types
    void visitBoolean(AntlrBooleanType type, T payload);

    void visitLong(AntlrLongType type, T payload);

    void visitString(AntlrStringType type, T payload);

    // Extension types
    void visitIpAddress(AntlrIpAddressType type, T payload);

    void visitDecimal(AntlrDecimalType type, T payload);

    void visitDateTime(AntlrDateTimeType type, T payload);

    void visitDuration(AntlrDurationType type, T payload);
}
