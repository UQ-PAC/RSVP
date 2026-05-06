package uq.pac.rsvp.policy.ast.schema.visitor;

import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.statement.Action;
import uq.pac.rsvp.policy.ast.schema.statement.CommonType;
import uq.pac.rsvp.policy.ast.schema.statement.EnumEntityType;
import uq.pac.rsvp.policy.ast.schema.statement.RecordEntityType;
import uq.pac.rsvp.policy.ast.schema.type.*;

public interface SchemaPayloadVisitor<T> {
    default void visitSchema(Schema schema, T payload) {
        throw new AssertionError();
    }

    // Statements
    default void visitRecordEntity(RecordEntityType entity, T payload) {
        throw new AssertionError();
    }

    default void visitEnumEntity(EnumEntityType entity, T payload) {
        throw new AssertionError();
    }

    default void visitAction(Action action, T payload) {
        throw new AssertionError();
    }

    default void visitCommon(CommonType type, T payload) {
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
