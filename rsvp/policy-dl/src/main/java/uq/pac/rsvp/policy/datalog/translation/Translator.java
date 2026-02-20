package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.datalog.ast.DLStatement;

import java.util.List;

/**
 * A translation component that accepts a Cedar construct and outputs
 * a collection of statements representing that construct at the datalog level
 */
public abstract class Translator {
    public abstract List<DLStatement> getTranslation();
}
