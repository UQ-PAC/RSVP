/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.datalog.ast;

/**
 * Superclass for a datalog term of a relation or a constraint
 * <code>
 *    Term ::= Variable | NUMBER | STRING
 * </code>
 */
public abstract class DLTerm extends DLNode {
    public static DLTerm var(String name) {
        return new DLVar(name);
    }

    public static DLTerm lit(String val) {
        return new DLString(val);
    }

    public static DLTerm lit(int val) {
        return new DLNumber(val);
    }

    public static DLTerm lit(double val) {
        return new DLNumber(val);
    }
}
