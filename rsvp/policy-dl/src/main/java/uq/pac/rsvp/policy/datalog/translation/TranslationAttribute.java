package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.datalog.ast.DLRelationDecl;

public class TranslationAttribute {
    private final String name;
    private final DLRelationDecl declaration;
    private final CommonTypeDefinition type;

    public TranslationAttribute(String name, CommonTypeDefinition type, DLRelationDecl declaration) {
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

    public CommonTypeDefinition getType() {
        return type;
    }

}
