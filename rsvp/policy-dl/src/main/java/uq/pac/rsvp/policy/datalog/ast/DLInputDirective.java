/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.datalog.ast;

/**
 * Input directive
 * <code>
 *   Directive ::= '.input' IDENT
 * </code>
 */
public class DLInputDirective extends DLDirective {
    public DLInputDirective(DLRuleDecl decl) {
        super(decl, "input");
    }
}
