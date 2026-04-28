package uq.pac.rsvp.policy.ast.antlrschema.statement;

import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaValueVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaVoidVisitor;
import uq.pac.rsvp.policy.ast.schema.common.RecordTypeDefinition;
import uq.pac.rsvp.support.SourceLoc;

public abstract class AntlrRecordEntityType extends AntlrSchemaStatement {

    private RecordTypeDefinition shape;

    public AntlrRecordEntityType(String namespace, String name, RecordTypeDefinition shape, SourceLoc location) {
        super(namespace, name, location);
        this.shape = shape;
    }

    public RecordTypeDefinition getShape() {
        return shape;
    }

    @Override
    public void accept(AntlrSchemaVoidVisitor visitor) {
        visitor.visitRecordEntity(this);
    }

    @Override
    public <T> T compute(AntlrSchemaValueVisitor<T> visitor) {
        return visitor.visitRecordEntity(this);
    }

    @Override
    public <T> void process(AntlrSchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitRecordEntity(this, payload);
    }
}
