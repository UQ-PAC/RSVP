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

public interface SchemaComputationVisitor<T> {
    public T visitSchema(Schema schema);

    public T visitNamespace(Namespace namespace);

    public T visitEntityType(EntityType type);

    public T visitAction(Action action);

    public T visitEntityOrCommonAttributeType(EntityOrCommonType type);

    public T visitExtensionAttributeType(ExtensionType type);

    public T visitPrimitiveAttributeType(PrimitiveType type);

    public T visitRecordAttributeType(RecordType type);

    public T visitSetAttributeType(SetType type);
}
