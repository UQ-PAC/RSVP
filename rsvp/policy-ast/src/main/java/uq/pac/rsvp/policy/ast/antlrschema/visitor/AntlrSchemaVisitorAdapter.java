package uq.pac.rsvp.policy.ast.antlrschema.visitor;

import uq.pac.rsvp.policy.ast.antlrschema.AntlrSchema;
import uq.pac.rsvp.policy.ast.antlrschema.statement.*;
import uq.pac.rsvp.policy.ast.antlrschema.type.*;

class AntlrSchemaVisitorAdapter implements AntlrSchemaVisitor {

    @Override
    public void visitSchema(AntlrSchema schema) {
        schema.statements().forEach(s -> s.accept(this));
    }

    @Override
    public void visitRecordEntity(AntlrRecordEntityType entity) {
        entity.getMemberOf().forEach(m -> m.accept(this));
        entity.getShape().accept(this);
    }

    @Override
    public void visitEnumEntity(AntlrEnumEntityType entity) { }

    @Override
    public void visitAction(AntlrAction action) {
        action.getMemberOf().forEach(r -> r.accept(this));
        AntlrActionApplication appliesTo = action.getApplication();
        appliesTo.getPrincipalTypes().forEach(p -> p.accept(this));
        appliesTo.getResourceTypes().forEach(p -> p.accept(this));
        appliesTo.getContext().accept(this);
    }

    @Override
    public void visitCommon(AntlrCommonType type) {
        type.getDefinition().accept(this);
    }

    @Override
    public void visitRecord(AntlrRecordType type) {
        type.getAttributes().values().forEach(t -> t.accept(this));
    }

    @Override
    public void visitSet(AntlrSetType type) {
        type.getElementType().accept(this);
    }

    @Override
    public void visitReference(AntlrTypeReference type) {}

    @Override
    public void visitBoolean(AntlrBooleanType type) {}

    @Override
    public void visitLong(AntlrLongType type) {}

    @Override
    public void visitString(AntlrStringType type) {}

    @Override
    public void visitIpAddress(AntlrIpAddressType type) {}

    @Override
    public void visitDecimal(AntlrDecimalType type) {}

    @Override
    public void visitDateTime(AntlrDateTimeType type) {}

    @Override
    public void visitDuration(AntlrDurationType type) {}
}
