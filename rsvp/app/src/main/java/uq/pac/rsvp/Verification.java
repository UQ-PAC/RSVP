package uq.pac.rsvp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;

import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.support.RsvpException;
import uq.pac.rsvp.support.reporting.Report;
import uq.pac.rsvp.support.reporting.Report.Severity;

public class Verification {

    public static List<Report> verifyPolicies(String filename, String policies) throws RsvpException {
        return verify(PolicySet.parseCedarPolicySet(filename, policies), null);
    }

    // public static List<Report> verify(String policies, String schema) throws
    // RsvpException {
    // return verify(PolicySet.parseCedarPolicySet(policies),
    // Schema.parseCedarSchema(schema));
    // }

    public static List<Report> verify(PolicySet policies, Schema schema) {
        List<Report> reports = new ArrayList<>();

        for (Policy policy : policies) {
            reports.add(
                    new Report(Severity.Info, "This is a fantastic policy. Well done Angus.", policy.getSourceLoc()));
        }

        // Gson gson = new Gson();
        return reports;
    }

}
