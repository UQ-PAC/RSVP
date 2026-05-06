package uq.pac.rsvp.policy.ast.schema.visitor;

import uq.pac.rsvp.policy.ast.schema.*;
import uq.pac.rsvp.policy.ast.schema.statement.Action;
import uq.pac.rsvp.policy.ast.schema.statement.CommonType;
import uq.pac.rsvp.policy.ast.schema.statement.EnumEntityType;
import uq.pac.rsvp.policy.ast.schema.statement.RecordEntityType;
import uq.pac.rsvp.policy.ast.schema.type.*;

public interface SchemaValueVisitor<T> {
    default T visitSchema(Schema schema) {
        throw new AssertionError();
    }

    // Statements
    default T visitRecordEntity(RecordEntityType entity) {
        throw new AssertionError();
    }

    default T visitEnumEntity(EnumEntityType entity) {
        throw new AssertionError();
    }

    default T visitAction(Action action) {
        throw new AssertionError();
    }

    default T visitCommon(CommonType type) {
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
