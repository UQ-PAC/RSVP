/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.verification;

import uq.pac.rsvp.policy.ast.policy.Policy;

import java.util.HashSet;
import java.util.Set;

public class RequestResult {
    public boolean permitted;
    public final Set<Policy> policies = new HashSet<>();
}
