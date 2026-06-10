package uq.pac.rsvp.policy.ast.schema.visitor;

import uq.pac.rsvp.policy.ast.schema.statement.ActionDefinition;
import uq.pac.rsvp.policy.ast.schema.statement.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.statement.EnumEntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.statement.RecordEntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.type.*;

import static uq.pac.rsvp.policy.ast.AstNode.unsupported;

public interface SchemaPayloadVisitor<T> {
    // Statements
    default void visitRecordEntity(RecordEntityTypeDefinition entity, T payload) {
        throw unsupported(entity);
    }

    default void visitEnumEntity(EnumEntityTypeDefinition entity, T payload) {
        throw unsupported(entity);
    }

    default void visitAction(ActionDefinition action, T payload) {
        throw unsupported(action);
    }

    default void visitCommon(CommonTypeDefinition type, T payload) {
        throw unsupported(type);
    }

    // Compound types
    default void visitRecord(RecordType type, T payload) {
        throw unsupported(type);
    }

    default void visitSet(SetType type, T payload) {
        throw unsupported(type);
    }

    // Type references
    default void visitTypeReference(TypeReference type, T payload) {
        throw unsupported(type);
    }

    // Primitive types
    default void visitBoolean(BooleanType type, T payload) {
        throw unsupported(type);
    }

    default void visitLong(LongType type, T payload) {
        throw unsupported(type);
    }

    default void visitString(StringType type, T payload) {
        throw unsupported(type);
    }

    // Extension types
    default void visitIpAddress(IpAddressType type, T payload) {
        throw unsupported(type);
    }

    default void visitDecimal(DecimalType type, T payload) {
        throw unsupported(type);
    }

    default void visitDateTime(DateTimeType type, T payload) {
        throw unsupported(type);
    }

    default void visitDuration(DurationType type, T payload) {
        throw unsupported(type);
    }
}
