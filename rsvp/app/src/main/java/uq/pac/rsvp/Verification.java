package uq.pac.rsvp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.ast.schema.Schema;
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

        Random r = new Random();

        for (Policy policy : policies) {
            int num = r.nextInt(100);
            System.err.println(policy.getName());
            reports.add(

                    new Report(num < 34 ? Severity.Info : num < 67 ? Severity.Warning : Severity.Error,
                            "This is a fantastic policy. Well done Angus.", policy.getSourceLoc()));
        }

        // Gson gson = new Gson();
        return reports;
    }

}
