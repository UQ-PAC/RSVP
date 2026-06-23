/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.datalog.ast;

/**
 * Input/output directive
 * <code>
 *   Directive ::= ('.input' | '.output') IDENT
 * </code>
 */
public abstract class DLDirective extends DLStatement {
    private final DLRuleDecl decl;
    private final String name;

    public DLDirective(DLRuleDecl decl, String directive) {
        this.decl = decl;
        this.name = directive;
    }

    public DLRuleDecl getDecl() {
        return decl;
    }

    @Override
    public String stringify() {
        return ".%s %s".formatted(name, decl.getName());
    }
}
