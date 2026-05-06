package uq.pac.rsvp.policy.ast.schema.statement;

import uq.pac.rsvp.policy.ast.schema.type.TypeReference;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaValueVisitor;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaVisitor;
import uq.pac.rsvp.support.SourceLoc;

import java.util.Set;

public class Action extends SchemaStatement {

    private final Set<TypeReference> memberOf;
    private final ActionApplication appliesTo;

    public Action(TypeReference ref, Set<TypeReference> memberOf, ActionApplication appliesTo, Annotations annotations, SourceLoc location) {
        super(ref, annotations, location);
        this.memberOf = memberOf;
        this.appliesTo = appliesTo;
    }

    @Override
    public String toString() {
        String in = memberOf.isEmpty() ? " " : " in " + memberOf + " ";
        return getAnnotations().toString() + "action " + getBaseName().replace("Action::", "") + in + appliesTo + ";";
    }

    public Set<TypeReference> getMemberOf() {
        return memberOf;
    }

    public ActionApplication getApplication() {
        return appliesTo;
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitAction(this);
    }

    @Override
    public <T> T compute(SchemaValueVisitor<T> visitor) {
        return visitor.visitAction(this);
    }

    @Override
    public <T> void process(SchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitAction(this, payload);
    }
}
