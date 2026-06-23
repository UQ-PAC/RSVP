/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.support.util;

import java.util.AbstractMap;

public class Pair<A, B> extends AbstractMap.SimpleEntry<A, B> {
    public Pair(A a, B b) {
        super(a, b);
    }
}
