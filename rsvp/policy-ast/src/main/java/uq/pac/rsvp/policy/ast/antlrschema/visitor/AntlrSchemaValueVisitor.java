package uq.pac.rsvp.policy.ast.antlrschema.visitor;

import uq.pac.rsvp.policy.ast.antlrschema.*;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrAction;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrCommonType;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrEnumEntityType;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrRecordEntityType;
import uq.pac.rsvp.policy.ast.antlrschema.type.*;

public interface AntlrSchemaValueVisitor<T> {
    default T visitSchema(AntlrSchema schema) {
        throw new AssertionError();
    }

    // Statements
    default T visitRecordEntity(AntlrRecordEntityType entity) {
        throw new AssertionError();
    }

    default T visitEnumEntity(AntlrEnumEntityType entity) {
        throw new AssertionError();
    }

    default T visitAction(AntlrAction action) {
        throw new AssertionError();
    }

    default T visitCommon(AntlrCommonType type) {
        throw new AssertionError();
    }

    // Compound types
    default T visitRecord(AntlrRecordType type) {
        throw new AssertionError();
    }

    default T visitSet(AntlrSetType type) {
        throw new AssertionError();
    }

    // Type references
    default T visitTypeReference(AntlrTypeReference type) {
        throw new AssertionError();
    }

    // Primitive types
    default T visitBoolean(AntlrBooleanType type) {
        throw new AssertionError();
    }

    default T visitLong(AntlrLongType type) {
        throw new AssertionError();
    }

    default T visitString(AntlrStringType type) {
        throw new AssertionError();
    }

    // Extension types
    default T visitIpAddress(AntlrIpAddressType type) {
        throw new AssertionError();
    }

    default T visitDecimal(AntlrDecimalType type) {
        throw new AssertionError();
    }

    default T visitDateTime(AntlrDateTimeType type) {
        throw new AssertionError();
    }

    default T visitDuration(AntlrDurationType type) {
        throw new AssertionError();
    }
}
