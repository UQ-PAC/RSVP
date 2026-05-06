package uq.pac.rsvp.policy.ast.schema.visitor;

import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.statement.*;
import uq.pac.rsvp.policy.ast.schema.type.*;

public class SchemaVisitorAdapter implements SchemaVisitor {

    @Override
    public void visitSchema(Schema schema) {
        schema.statements().forEach(s -> s.accept(this));
    }

    @Override
    public void visitRecordEntity(RecordEntityType entity) {
        entity.getMemberOf().forEach(m -> m.accept(this));
        entity.getShape().accept(this);
    }

    @Override
    public void visitEnumEntity(EnumEntityType entity) { }

    @Override
    public void visitAction(Action action) {
        action.getMemberOf().forEach(r -> r.accept(this));
        ActionApplication appliesTo = action.getApplication();
        appliesTo.getPrincipalTypes().forEach(p -> p.accept(this));
        appliesTo.getResourceTypes().forEach(p -> p.accept(this));
        appliesTo.getContext().accept(this);
    }

    @Override
    public void visitCommon(CommonType type) {
        type.getDefinition().accept(this);
    }

    @Override
    public void visitRecord(RecordType type) {
        type.getAttributes().values().forEach(t -> t.accept(this));
    }

    @Override
    public void visitSet(SetType type) {
        type.getElementType().accept(this);
    }

    @Override
    public void visitTypeReference(TypeReference type) {}

    @Override
    public void visitBoolean(BooleanType type) {}

    @Override
    public void visitLong(LongType type) {}

    @Override
    public void visitString(StringType type) {}

    @Override
    public void visitIpAddress(IpAddressType type) {}

    @Override
    public void visitDecimal(DecimalType type) {}

    @Override
    public void visitDateTime(DateTimeType type) {}

    @Override
    public void visitDuration(DurationType type) {}
}
