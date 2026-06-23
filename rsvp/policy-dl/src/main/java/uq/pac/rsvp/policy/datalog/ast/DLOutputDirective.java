/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.datalog.ast;

/**
 * Output directive
 * <code>
 *   Directive ::= '.output' IDENT
 * </code>
 */
public class DLOutputDirective extends DLDirective {
    public DLOutputDirective(DLRuleDecl decl) {
        super(decl, "output");
    }
}
