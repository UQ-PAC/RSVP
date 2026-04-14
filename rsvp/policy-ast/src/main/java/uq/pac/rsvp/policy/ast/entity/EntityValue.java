package uq.pac.rsvp.policy.ast.entity;

import uq.pac.rsvp.support.SourceLoc;

public abstract class EntityValue {
    protected final SourceLoc location;

    public EntityValue(SourceLoc location) {
        this.location = location;
    }

    public SourceLoc getLocation() {
        return location;
    }
}
