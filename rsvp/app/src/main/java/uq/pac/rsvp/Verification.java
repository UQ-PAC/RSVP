package uq.pac.rsvp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.ast.expr.Expression;
import uq.pac.rsvp.policy.ast.expr.BinaryExpression;
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

            // if (i % 4 == 3) {
            // i++;
            // continue;
            // }

            int severity = r.nextInt(100);
            int detail = r.nextInt(100);
            int loc = r.nextInt(100);

            if (i < 2) {
                locations[i] = policy.getSourceLoc();
            }

            Expression cond = policy.getCondition();

            // if (cond instanceof BinaryExpression) {
            //     cond = ((BinaryExpression) cond).getLeft();
            // }

            reports.add(

                    new Report(severity < 34 ? Severity.Info : severity < 67 ? Severity.Warning : Severity.Error,
                            loc < 50 ? "This is a fantastic policy. Well done Angus."
                                    : "This is an amazing policy condition. Congratulations.",
                            cond.getSourceLoc() == SourceLoc.MISSING ? cond.toString()
                                    : detail < 50 ? ""
                                            : "This is a very detailed report on your policy. Your policy is truly a thing of beauty, and we should all aspire to emulate this policy.",
                            loc < 50 ? policy.getSourceLoc() : cond.getSourceLoc(),
                            i == 2 ? locations : new SourceLoc[0]));
            i++;
        }

        return reports;
    }

}
