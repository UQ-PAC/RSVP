package uq.pac.rsvp.policy.ast.schema;

import uq.pac.rsvp.policy.ast.AstNode;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaVisitor;
import uq.pac.rsvp.support.SourceLoc;

public abstract class SchemaAstNode extends AstNode {
    public SchemaAstNode(SourceLoc location) {
        super(location);
    }

    public abstract void accept(SchemaVisitor visitor);

    public abstract <T> T compute(SchemaComputationVisitor<T> visitor);

    public abstract <T> void process(SchemaPayloadVisitor<T> visitor, T payload);
}
