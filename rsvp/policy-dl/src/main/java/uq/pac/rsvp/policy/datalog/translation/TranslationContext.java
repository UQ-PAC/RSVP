/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.datalog.translation;

/**
 * Context of translation, i.e., whether we translate policies or invariants
 */
public enum TranslationContext {
	// Translating Cedar Policies
    Policy,
	// Translating invariants
    Invariant
}
