package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.datalog.ast.DLRuleDecl;

public class TranslationAttribute {
    private final String name;
    private final DLRuleDecl declaration;
    private final CommonTypeDefinition type;

    public TranslationAttribute(String name, CommonTypeDefinition type, DLRuleDecl declaration) {
        this.name = name;
        this.type = type;
        this.declaration = declaration;
    }

    public String getName() {
        return name;
    }

    public DLRuleDecl getRuleDecl() {
        return declaration;
    }

    public CommonTypeDefinition getType() {
        return type;
    }

}
