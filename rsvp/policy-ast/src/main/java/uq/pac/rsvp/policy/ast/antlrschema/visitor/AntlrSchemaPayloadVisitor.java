package uq.pac.rsvp.policy.ast.antlrschema.visitor;

import uq.pac.rsvp.policy.ast.antlrschema.AntlrSchema;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrAction;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrCommonType;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrEnumEntityType;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrRecordEntityType;
import uq.pac.rsvp.policy.ast.antlrschema.type.*;

public interface AntlrSchemaPayloadVisitor<T> {
    default void visitSchema(AntlrSchema schema, T payload) {
        throw new AssertionError();
    }

    // Statements
    default void visitRecordEntity(AntlrRecordEntityType entity, T payload) {
        throw new AssertionError();
    }

    default void visitEnumEntity(AntlrEnumEntityType entity, T payload) {
        throw new AssertionError();
    }

    default void visitAction(AntlrAction action, T payload) {
        throw new AssertionError();
    }

    default void visitCommon(AntlrCommonType type, T payload) {
        throw new AssertionError();
    }

    // Compound types
    default void visitRecord(AntlrRecordType type, T payload) {
        throw new AssertionError();
    }

    default void visitSet(AntlrSetType type, T payload) {
        throw new AssertionError();
    }

    // Type references
    default void visitTypeReference(AntlrTypeReference type, T payload) {
        throw new AssertionError();
    }

    // Primitive types
    default void visitBoolean(AntlrBooleanType type, T payload) {
        throw new AssertionError();
    }

    default void visitLong(AntlrLongType type, T payload) {
        throw new AssertionError();
    }

    default void visitString(AntlrStringType type, T payload) {
        throw new AssertionError();
    }

    // Extension types
    default void visitIpAddress(AntlrIpAddressType type, T payload) {
        throw new AssertionError();
    }

    default void visitDecimal(AntlrDecimalType type, T payload) {
        throw new AssertionError();
    }

    default void visitDateTime(AntlrDateTimeType type, T payload) {
        throw new AssertionError();
    }

    default void visitDuration(AntlrDurationType type, T payload) {
        throw new AssertionError();
    }
}
