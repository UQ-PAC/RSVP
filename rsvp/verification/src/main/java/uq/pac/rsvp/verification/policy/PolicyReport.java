package uq.pac.rsvp.verification.policy;

import uq.pac.rsvp.policy.ast.policy.Invariant;
import uq.pac.rsvp.policy.ast.policy.Policy;
import uq.pac.rsvp.support.reporting.Report;

// TODO: detect more than two policies if relevant
public abstract class PolicyReport {

    public static class SubsumedPolicy extends Report {

        public SubsumedPolicy(Policy subsumed, Policy subsumer) {

            super(Report.Severity.Warning,
                    "Subsumed Policy",
                    "Every request " + getActionAsVerb(subsumed, true) + " by this policy is already " + getActionAsVerb(subsumer, true) + " by a second policy.",
                    new Report.LocationMessage(subsumed.getSourceLoc(), "Subsumed policy"),
                    new Report.LocationMessage(subsumer.getSourceLoc(), "Is subsumed by this policy")
            );

        }

        public SubsumedPolicy(Policy subsumed) {
            super(Report.Severity.Warning,
                    "Subsumed Policy",
                    "Policy does not " + getActionAsVerb(subsumed, false) + " anything that is not already "
                            + getActionAsVerb(subsumed, true)
                            + ", and can be removed with no impact.",
                    subsumed.getSourceLoc());

        }
    }

    public static class IdenticalPolicies extends Report {
        public IdenticalPolicies(Policy one, Policy two) {
            super(Report.Severity.Warning,
                    "Duplicated Policy",
                    "There is no difference in what is " + getActionAsVerb(one, true) + " by these two policies.",
                    new Report.LocationMessage(one.getSourceLoc(), ""),
                    new Report.LocationMessage(two.getSourceLoc(), "")
            );
        }
    }

    public static class ContradictoryPolicies extends Report {
        public ContradictoryPolicies(Policy permitPolicy, Policy forbidPolicy, boolean exact) {
            super(Report.Severity.Warning,
                    "Contradicted Policy",
                    "The first of these policies forbids " + (exact ? "exactly what" : "everything that")
                            +  " is permitted by the second",
                    new Report.LocationMessage(forbidPolicy.getSourceLoc(), ""),
                    new Report.LocationMessage(permitPolicy.getSourceLoc(), "")
            );
        }
    }

    public static class UnusedPolicy extends Report {
        public UnusedPolicy(Policy policy) {
            super(Report.Severity.Warning,
                    "Unused Policy",
                    "Policy does not actually " + getActionAsVerb(policy, false) + " anything.",
                    policy.getSourceLoc());
        }
    }

    public static class InvariantNotHeld extends Report {
        public InvariantNotHeld(Invariant invariant, String counterexamples) {
            super(Report.Severity.Error, "Invariant does not hold", counterexamples, invariant.getSourceLoc());

        }
    }

    private static String getActionAsVerb(Policy policy, boolean past) {
        return policy.isPermit() ? (past ? "permitted" : "permit") : (past ? "forbidden" : "forbid");
    }
}
