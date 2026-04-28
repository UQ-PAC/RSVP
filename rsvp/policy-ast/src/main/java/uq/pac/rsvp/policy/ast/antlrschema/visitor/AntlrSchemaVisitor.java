package uq.pac.rsvp.policy.ast.antlrschema.visitor;

import uq.pac.rsvp.policy.ast.antlrschema.*;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrAction;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrCommonType;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrEnumEntityType;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrRecordEntityType;
import uq.pac.rsvp.policy.ast.antlrschema.type.*;

public interface AntlrSchemaVisitor {
    void visitSchema(AntlrSchema schema);

    // Statements
    void visitRecordEntity(AntlrRecordEntityType entity);

    void visitEnumEntity(AntlrEnumEntityType entity);

    void visitAction(AntlrAction action);

    void visitCommon(AntlrCommonType type);

    // Compound types
    void visitRecord(AntlrRecordType type);

    void visitSet(AntlrSetType type);

    // Type references
    void visitReference(AntlrTypeReference type);

    // Primitive types
    void visitBoolean(AntlrBooleanType type);

    void visitLong(AntlrLongType type);

    void visitString(AntlrStringType type);

    // Extension types
    void visitIpAddress(AntlrIpAddressType type);

    void visitDecimal(AntlrDecimalType type);

    void visitDateTime(AntlrDateTimeType type);

    void visitDuration(AntlrDurationType type);
}
