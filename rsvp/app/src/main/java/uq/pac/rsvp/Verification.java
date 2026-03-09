package uq.pac.rsvp;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.cedarpolicy.model.exception.InternalException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.gson.Gson;

import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.support.reporting.Report;
import uq.pac.rsvp.support.reporting.Report.Severity;

public class Verification {

    public String verify(String policyFile, String policies, String schemaFile, String schema) throws RsvpException {
        return verify(PolicySet.parseCedarPolicySet(policies), Schema.parseCedarSchema(schema));
    }

    public String verify(PolicySet policies, Schema schema) {
        Set<Report> reports = new HashSet<>();

        for (Policy policy : policies) {
            reports.add(new Report(Severity.Info, policy.getName(), policy.getSourceLoc()));
        }

        Gson gson = new Gson();
        return gson.toJson(reports, HashSet.class);
    }

}
