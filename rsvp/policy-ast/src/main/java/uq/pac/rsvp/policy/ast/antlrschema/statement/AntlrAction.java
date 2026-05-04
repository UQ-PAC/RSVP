package uq.pac.rsvp.policy.ast.antlrschema.statement;

import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrTypeReference;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaValueVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaVisitor;
import uq.pac.rsvp.support.SourceLoc;

import java.util.Set;

public class AntlrAction extends AntlrSchemaStatement {

    private final Set<AntlrTypeReference> memberOf;
    private final AntlrActionApplication appliesTo;

    public AntlrAction(AntlrTypeReference ref, Set<AntlrTypeReference> memberOf, AntlrActionApplication appliesTo, SourceLoc location) {
        super(ref, location);
        this.memberOf = memberOf;
        this.appliesTo = appliesTo;
    }

    @Override
    public String toString() {
        String in = memberOf.isEmpty() ? " " : " in " + memberOf + " ";
        return "action " + getBaseName().replace("Action::", "") + in + appliesTo + ";";
    }

    public Set<AntlrTypeReference> getMemberOf() {
        return memberOf;
    }

    public AntlrActionApplication getApplication() {
        return appliesTo;
    }

    @Override
    public void accept(AntlrSchemaVisitor visitor) {
        visitor.visitAction(this);
    }

    @Override
    public <T> T compute(AntlrSchemaValueVisitor<T> visitor) {
        return visitor.visitAction(this);
    }

    @Override
    public <T> void process(AntlrSchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitAction(this, payload);
    }
}
