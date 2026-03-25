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
