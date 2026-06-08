package uq.pac.rsvp.policy.ast.schema.visitor;

import uq.pac.rsvp.policy.ast.schema.statement.ActionDefinition;
import uq.pac.rsvp.policy.ast.schema.statement.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.statement.EnumEntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.statement.RecordEntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.type.*;

public interface SchemaComputationVisitor<T> {
    // Statements
    default T visitRecordEntity(RecordEntityTypeDefinition entity) {
        throw new AssertionError();
    }

    default T visitEnumEntity(EnumEntityTypeDefinition entity) {
        throw new AssertionError();
    }

    default T visitAction(ActionDefinition action) {
        throw new AssertionError();
    }

    default T visitCommon(CommonTypeDefinition type) {
        throw new AssertionError();
    }

    // Compound types
    default T visitRecord(RecordType type) {
        throw new AssertionError();
    }

    default T visitSet(SetType type) {
        throw new AssertionError();
    }

    // Type references
    default T visitTypeReference(TypeReference type) {
        throw new AssertionError();
    }

    // Primitive types
    default T visitBoolean(BooleanType type) {
        throw new AssertionError();
    }

    default T visitLong(LongType type) {
        throw new AssertionError();
    }

    default T visitString(StringType type) {
        throw new AssertionError();
    }

    // Extension types
    default T visitIpAddress(IpAddressType type) {
        throw new AssertionError();
    }

    default T visitDecimal(DecimalType type) {
        throw new AssertionError();
    }

    default T visitDateTime(DateTimeType type) {
        throw new AssertionError();
    }

    default T visitDuration(DurationType type) {
        throw new AssertionError();
    }
}
