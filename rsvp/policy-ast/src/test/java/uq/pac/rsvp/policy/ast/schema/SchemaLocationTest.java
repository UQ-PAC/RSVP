/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.ast.schema;

import org.junit.jupiter.api.Test;
import uq.pac.rsvp.policy.ast.AstNode;
import uq.pac.rsvp.policy.ast.TestUtil;
import uq.pac.rsvp.policy.ast.schema.statement.*;
import uq.pac.rsvp.policy.ast.schema.type.*;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaVisitor;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

public class SchemaLocationTest {

    @Test
    void test() throws IOException {
        String filename = "schema/location/photoapp.cedarschema";
        URL url = ClassLoader.getSystemResource(filename);
        Path path = Path.of(url.getPath());
        Schema schema = Schema.parse("file", Files.readString(path));
        Visitor visitor = new Visitor();
        schema.statements()
                .sorted(Comparator.comparingInt(a -> a.getSourceLoc().offset))
                .forEach(s -> s.accept(visitor));
        Path expectedPath =
                Path.of(ClassLoader.getSystemResource(filename + ".expected").getPath());

        if (TestUtil.GENERATE_ORACLES) {
            Files.writeString(expectedPath, visitor.getData());
        }
        String expected = Files.readString(expectedPath);
        assertEquals(expected.trim(), visitor.getData().trim());
    }

    static class Visitor implements SchemaVisitor {

        private final StringBuilder sb = new StringBuilder();
        private int indent = 0;

        String getData() {
            return sb.toString();
        }

        void log(String msg, AstNode node, Runnable consumer) {
            indent += 3;
            log("%s at %s".formatted(msg, node.getSourceLoc().toString()));
            consumer.run();
            assertFalse(node.getSourceLoc().isEmpty());
            indent -= 3;
        }

        void log(String msg) {
            if (!sb.isEmpty()) {
                sb.append('\n');
            }
            sb.repeat(" ", Math.max(0, indent));
            sb.append(msg);
        }

        @Override
        public void visitRecordEntity(RecordEntityTypeDefinition entity) {
            log("Record Entity " + entity.getTypeReference().toString(), entity, () -> {
                entity.getTypeReference().accept(this);
                entity.getMemberOf().forEach(m -> m.accept(this));
                entity.getShape().accept(this);
            });
        }

        @Override
        public void visitEnumEntity(EnumEntityTypeDefinition entity) {
            log("Enum Entity " + entity.getTypeReference().toString(), entity, () -> {
                entity.getTypeReference().accept(this);
                entity.getMemberOf().forEach(m -> m.accept(this));
            });
        }

        @Override
        public void visitAction(ActionDefinition action) {
            log("Action " + action.getTypeReference().toString(), action, () -> {
                action.getTypeReference().accept(this);
                action.getMemberOf().forEach(m -> m.accept(this));
                action.getApplication().getContext().accept(this);
                action.getApplication().getPrincipalTypes().forEach(p -> p.accept(this));
                action.getApplication().getResourceTypes().forEach(p -> p.accept(this));
            });
        }

        @Override
        public void visitCommon(CommonTypeDefinition type) {
            log("Type " + type.getTypeReference().toString(), type, () -> {
                type.getTypeReference().accept(this);
                type.getDefinition().accept(this);
            });
        }

        @Override
        public void visitRecord(RecordType type) {
            log("Record", type, () -> {
                type.getAttributes().keySet()
                        .stream()
                        .sorted(Comparator.comparing(RecordType.Attribute::toString))
                        .forEach(attr -> {
                            log("Attribute " + attr);
                            type.getAttribute(attr.getName()).accept(this);
                        });
            });
        }

        @Override
        public void visitSet(SetType type) {
            log("Set", type, () -> type.getElementType().accept(this));
        }

        @Override
        public void visitTypeReference(TypeReference type) {
            log("Reference " + type.toString(), type, () -> {});
        }

        @Override
        public void visitBoolean(BooleanType type) {
            log(type.toString(), type, () -> {});
        }

        @Override
        public void visitLong(LongType type) {
            log(type.toString(), type, () -> {});
        }

        @Override
        public void visitString(StringType type) {
            log(type.toString(), type, () -> {});
        }

        @Override
        public void visitIpAddress(IpAddressType type) {
            log(type.toString(), type, () -> {});
        }

        @Override
        public void visitDecimal(DecimalType type) {
            log(type.toString(), type, () -> {});
        }

        @Override
        public void visitDateTime(DateTimeType type) {
            log(type.toString(), type, () -> {});
        }

        @Override
        public void visitDuration(DurationType type) {
            log(type.toString(), type, () -> {});
        }
    }
}