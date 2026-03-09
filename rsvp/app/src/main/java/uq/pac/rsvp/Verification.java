package uq.pac.rsvp;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;

import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.support.reporting.Report;
import uq.pac.rsvp.support.reporting.Report.Severity;

public class Verification {

    public static String helloWorld() {
        return "Hello world!";
    }

    public static String verify(String policies, String schema) throws RsvpException {
        return verify(PolicySet.parseCedarPolicySet(policies), Schema.parseCedarSchema(schema));
    }

    public static String verify(PolicySet policies, Schema schema) {
        Set<Report> reports = new HashSet<>();

        for (Policy policy : policies) {
            reports.add(new Report(Severity.Info, policy.getName(), policy.getSourceLoc()));
        }

        Gson gson = new Gson();
        return gson.toJson(reports, HashSet.class);
    }

}
