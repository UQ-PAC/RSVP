/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.datalog.ast;

import com.google.gson.Gson;

/**
 * String literal term (double-quoted)
 */
public final class DLString extends DLTerm {
    private final static Gson GSON = new Gson();
    private final String value;

    public DLString(String value) {
        this.value = value;
    }

    @Override
    protected String stringify() {
        return GSON.toJson(value);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DLString s) {
            return s.value.equals(value);
        }
        return false;
    }

    public String getValue() {
        return value;
    }
}
