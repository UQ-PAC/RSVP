package uq.pac.rsvp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.support.SourceLoc;
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

        SourceLoc[] locations = new SourceLoc[2];
        int i = 0;

        for (Policy policy : policies) {
            int num = r.nextInt(100);
            int detail = r.nextInt(100);
            if (i < 2) {
                locations[i] = policy.getSourceLoc();
            }

            reports.add(

                    new Report(num < 34 ? Severity.Info : num < 67 ? Severity.Warning : Severity.Error,
                            "This is a fantastic policy. Well done Angus.",
                            detail < 50 ? ""
                                    : "This is a very detailed report on your policy. Your policy is truly a thing of beauty, and we should all aspire to emulate this policy.",
                            policy.getSourceLoc(), i == 2 ? locations : new SourceLoc[0]));
            i++;
        }

        return reports;
    }

}
