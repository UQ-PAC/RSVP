package uq.pac.rsvp.policy.ast.entity;

import com.google.gson.JsonElement;
import uq.pac.rsvp.policy.ast.AstNode;
import uq.pac.rsvp.support.SourceLoc;

public abstract class EntityValue extends AstNode {

    public EntityValue(SourceLoc location) {
        super(location);
    }

    public abstract JsonElement toJson();
}
