package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.value.EntityTypeName;
import uq.pac.rsvp.policy.ast.schema.attribute.AttributeType;
import uq.pac.rsvp.policy.datalog.ast.DLRelationDecl;

public class TranslationAttribute {
    private final String name;
    /**
     * Declaration of the datalog relation mapping entities to attributes
     */
    private final DLRelationDecl declaration;

    private final AttributeType type;

    public TranslationAttribute(String name, AttributeType type, DLRelationDecl declaration) {
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

    public AttributeType getType() {
        return type;
    }

}
