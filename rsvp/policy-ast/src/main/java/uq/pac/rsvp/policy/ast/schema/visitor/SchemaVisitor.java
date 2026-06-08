package uq.pac.rsvp.policy.ast.schema.visitor;

import uq.pac.rsvp.policy.ast.schema.*;
import uq.pac.rsvp.policy.ast.schema.statement.ActionDefinition;
import uq.pac.rsvp.policy.ast.schema.statement.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.statement.EnumEntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.statement.RecordEntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.type.*;

public interface SchemaVisitor {
    // Statements
    default void visitRecordEntity(RecordEntityTypeDefinition entity) {
        throw new AssertionError();
    }

    default void visitEnumEntity(EnumEntityTypeDefinition entity) {
        throw new AssertionError();
    }

    default void visitAction(ActionDefinition action) {
        throw new AssertionError();
    }

    default void visitCommon(CommonTypeDefinition type) {
        throw new AssertionError();
    }

    // Compound types
    default void visitRecord(RecordType type) {
        throw new AssertionError();
    }

    default void visitSet(SetType type) {
        throw new AssertionError();
    }

    // Type references
    default void visitTypeReference(TypeReference type) {
        throw new AssertionError();
    }

    // Primitive types
    default void visitBoolean(BooleanType type) {
        throw new AssertionError();
    }

    default void visitLong(LongType type) {
        throw new AssertionError();
    }

    default void visitString(StringType type) {
        throw new AssertionError();
    }

    // Extension types
    default void visitIpAddress(IpAddressType type) {
        throw new AssertionError();
    }

    default void visitDecimal(DecimalType type) {
        throw new AssertionError();
    }

    default void visitDateTime(DateTimeType type) {
        throw new AssertionError();
    }

    default void visitDuration(DurationType type) {
        throw new AssertionError();
    }
}
