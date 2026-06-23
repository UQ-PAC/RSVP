/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.datalog.ast;

import static uq.pac.rsvp.Assertion.require;

/**
 * Datalog fact
 * <code>
 *   Fact := Atom '.'
 * </code>
 */
public class DLFact extends DLStatement {
    private final DLAtom atom;

    public DLFact(DLRuleDecl decl, DLTerm ...terms) {
        this.atom = new DLAtom(decl, terms);
        require(!atom.isNegated());
    }

    protected String stringify() {
        return atom + ".";
    }

    public DLAtom getAtom() {
        return atom;
    }

    public String getName() {
        return atom.getName();
    }
}
