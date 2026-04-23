package uq.pac.rsvp.policy.ast.expr;

import uq.pac.rsvp.support.SourceLoc;

public abstract class EuidExpression extends Expression {
    private final String entityType;
    private final String eid;

    protected EuidExpression(String eid, String entityType, SourceLoc source) {
        super(source);
        this.entityType = entityType;
        this.eid = eid;
    }

    /**
     * Get the type of this entity in the format {@code Namespace::Type}.
     * 
     * @return the qualified type of this entity
     */
    public final String getType() {
        return entityType;
    }

    /**
     * Get the unquoted, unqualified EID of this entity.
     * 
     * @return the EID of this entity
     */
    public final String getName() {
        return eid;
    }

    /**
     * Return the fully qualified name of this entity in the format
     * {@code Namespace::Type::"entityName"}
     * 
     * @return The fully qualified name of this entity
     */
    public final String getQualifiedName() {
        return entityType + "::\"" + eid + "\"";
    }

    @Override
    public final String toString() {
        return getQualifiedName();
    }
}
