/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.ast.schema.visitor;

import uq.pac.rsvp.policy.ast.schema.statement.ActionDefinition;
import uq.pac.rsvp.policy.ast.schema.statement.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.statement.EnumEntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.statement.RecordEntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.type.*;

import static uq.pac.rsvp.policy.ast.AstNode.unsupported;

public interface SchemaVisitor {
    // Statements
    default void visitRecordEntity(RecordEntityTypeDefinition entity) {
        throw unsupported(entity);
    }

    default void visitEnumEntity(EnumEntityTypeDefinition entity) {
        throw unsupported(entity);
    }

    default void visitAction(ActionDefinition action) {
        throw unsupported(action);
    }

    default void visitCommon(CommonTypeDefinition type) {
        throw unsupported(type);
    }

    // Compound types
    default void visitRecord(RecordType type) {
        throw unsupported(type);
    }

    default void visitSet(SetType type) {
        throw unsupported(type);
    }

    // Type references
    default void visitTypeReference(TypeReference type) {
        throw unsupported(type);
    }

    // Primitive types
    default void visitBoolean(BooleanType type) {
        throw unsupported(type);
    }

    default void visitLong(LongType type) {
        throw unsupported(type);
    }

    default void visitString(StringType type) {
        throw unsupported(type);
    }

    // Extension types
    default void visitIpAddress(IpAddressType type) {
        throw unsupported(type);
    }

    default void visitDecimal(DecimalType type) {
        throw unsupported(type);
    }

    default void visitDateTime(DateTimeType type) {
        throw unsupported(type);
    }

    default void visitDuration(DurationType type) {
        throw unsupported(type);
    }
}
