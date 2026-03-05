package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.EntityTypeDefinition;
import uq.pac.rsvp.policy.datalog.ast.DLRuleDecl;

/**
 * A class representing at attribute relation (see {@link EntityTypeDefinition})
 * that tracks the name of the attribute, datalog declaration and the type of the
 * attribute as per AST.
 */
public class TranslationAttribute {
    /**
     * Attribute name
     */
    private final String name;
    /**
     * Binary declaration mapping entities to attributes
     */
    private final DLRuleDecl declaration;
    /**
     * AST type-definition of the attribute (per Cedar schema)
     */
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
