package uq.pac.rsvp.policy.ast.visitor;

import uq.pac.rsvp.policy.ast.schema.Action;
import uq.pac.rsvp.policy.ast.schema.EntityType;
import uq.pac.rsvp.policy.ast.schema.Namespace;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.attribute.EntityOrCommonType;
import uq.pac.rsvp.policy.ast.schema.attribute.ExtensionType;
import uq.pac.rsvp.policy.ast.schema.attribute.PrimitiveType;
import uq.pac.rsvp.policy.ast.schema.attribute.RecordType;
import uq.pac.rsvp.policy.ast.schema.attribute.SetType;

public interface SchemaVisitor {
    public void visitSchema(Schema schema);

    public void visitNamespace(Namespace namespace);

    public void visitEntityType(EntityType type);

    public void visitAction(Action action);

    public void visitEntityOrCommonAttributeType(EntityOrCommonType type);

    public void visitExtensionAttributeType(ExtensionType type);

    public void visitPrimitiveAttributeType(PrimitiveType type);

    public void visitRecordAttributeType(RecordType type);

    public void visitSetAttributeType(SetType type);
}
