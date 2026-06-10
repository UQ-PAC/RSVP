package uq.pac.rsvp.policy.ast.schema.visitor;

import uq.pac.rsvp.policy.ast.schema.statement.ActionDefinition;
import uq.pac.rsvp.policy.ast.schema.statement.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.statement.EnumEntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.statement.RecordEntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.type.*;

public interface SchemaPayloadVisitor<T> {
    // Statements
    default void visitRecordEntity(RecordEntityTypeDefinition entity, T payload) {
        throw new AssertionError();
    }

    default void visitEnumEntity(EnumEntityTypeDefinition entity, T payload) {
        throw new AssertionError();
    }

    default void visitAction(ActionDefinition action, T payload) {
        throw new AssertionError();
    }

    default void visitCommon(CommonTypeDefinition type, T payload) {
        throw new AssertionError();
    }

    // Compound types
    default void visitRecord(RecordType type, T payload) {
        throw new AssertionError();
    }

    default void visitSet(SetType type, T payload) {
        throw new AssertionError();
    }

    // Type references
    default void visitTypeReference(TypeReference type, T payload) {
        throw new AssertionError();
    }

    // Primitive types
    default void visitBoolean(BooleanType type, T payload) {
        throw new AssertionError();
    }

    default void visitLong(LongType type, T payload) {
        throw new AssertionError();
    }

    default void visitString(StringType type, T payload) {
        throw new AssertionError();
    }

    // Extension types
    default void visitIpAddress(IpAddressType type, T payload) {
        throw new AssertionError();
    }

    default void visitDecimal(DecimalType type, T payload) {
        throw new AssertionError();
    }

    default void visitDateTime(DateTimeType type, T payload) {
        throw new AssertionError();
    }

    default void visitDuration(DurationType type, T payload) {
        throw new AssertionError();
    }
}
