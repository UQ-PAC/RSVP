package uq.pac.rsvp.policy.ast.entity;

import uq.pac.rsvp.support.SourceLoc;

/**
 * Exception thrown during processing of entities
 */
public class EntityException extends RuntimeException {
    public EntityException(SourceLoc loc, String msg) {
        super(msg + (loc == null || loc == SourceLoc.MISSING ? "" : "\n    at " + loc));
    }
}
