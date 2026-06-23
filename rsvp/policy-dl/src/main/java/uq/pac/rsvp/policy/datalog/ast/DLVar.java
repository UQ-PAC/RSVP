/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.datalog.ast;

/**
 * Datalog variable:
 * <code>
 *   Variable ::= IDENT
 * </code>
 */
public final class DLVar extends DLTerm {
    private final String name;

    public DLVar(String name) {
        this.name = name;
    }

    @Override
    protected String stringify() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DLVar s) {
            return s.name.equals(name);
        }
        return false;
    }

    public String getName() {
        return name;
    }
}
