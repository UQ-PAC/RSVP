package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.value.EntityTypeName;
import uq.pac.rsvp.policy.datalog.ast.DLRelationDecl;

public class TranslationAttribute {
    private final String name;
    /**
     * Declaration of the datalog relation mapping entities to attributes
     */
    private final DLRelationDecl declaration;
    /**
     * Reference to an entity type (potentially) Could be null
     * FIXME: Should use types from the schema when ready
     * FIXME: Can also be a record
     */
    private final EntityTypeName type;


    public TranslationAttribute(String name, EntityTypeName type) {
        this(name, type, null);
    }

    public TranslationAttribute(String name, EntityTypeName type, DLRelationDecl declaration) {
        this.name = name;
        this.type = type;
        this.declaration = declaration;
    }

    public String getName() {
        return name;
    }

    public DLRelationDecl getRelationDecl() {
        return declaration;
    }

    public EntityTypeName getType() {
        return type;
    }

}
