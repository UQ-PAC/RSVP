package uq.pac.rsvp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicyFileEntry;
import uq.pac.rsvp.policy.ast.PolicySet;
import uq.pac.rsvp.policy.ast.expr.BinaryExpression;
import uq.pac.rsvp.policy.ast.expr.CallExpression;
import uq.pac.rsvp.policy.ast.expr.ConditionalExpression;
import uq.pac.rsvp.policy.ast.expr.PropertyAccessExpression;
import uq.pac.rsvp.policy.ast.expr.RecordExpression;
import uq.pac.rsvp.policy.ast.expr.SetExpression;
import uq.pac.rsvp.policy.ast.expr.UnaryExpression;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitorImpl;
import uq.pac.rsvp.policy.datalog.translation.RequestSet;
import uq.pac.rsvp.policy.datalog.translation.Translation;
import uq.pac.rsvp.support.SourceLoc;
import uq.pac.rsvp.support.reporting.Report;
import uq.pac.rsvp.support.reporting.Report.Severity;

public class Verification {

    public static Set<Report> verifyPolicies(String policyFilename, Path policies, Path schema, Path entities)
            throws RsvpException, IOException {

        Set<Report> results = new HashSet<>();

        Path dlPath = Files.createTempDirectory("rsvp-");

        Translation translation =
                new Translation(schema, policies, entities, null, dlPath);

        Map<Policy, RequestSet> policyResults = translation.getPolicyResult();

        policyResults.forEach((k, v) -> {
            if (v.isEmpty()) {
                Report r = new Report(Severity.Warning, "Policy '" + k.getName() + "' does not match any requests",
                        k.getSourceLoc());
                results.add(r);
            }
        });

        return results;
    }

    // public static List<Report> verify(String policies, String schema) throws
    // RsvpException {
    // return verify(PolicySet.parseCedarPolicySet(policies),
    // Schema.parseCedarSchema(schema));
    // }

    public static Set<Report> verify(PolicySet policies, Schema schema) {
        RandomReportGenerator generator = new RandomReportGenerator();

        policies.accept(generator);

        return generator.reports;
    }

    private static class RandomReportGenerator extends PolicyVisitorImpl {

        public final Set<Report> reports;

        private final Random random;

        RandomReportGenerator() {
            reports = new HashSet<>();
            random = new Random();
        }

        @Override
        public void visitPolicy(Policy policy) {
            maybeAddRandomReport(policy, 20);
            super.visitPolicy(policy);
        }

        @Override
        public void visitBinaryExpr(BinaryExpression expr) {
            maybeAddRandomReport(expr, 5);
            super.visitBinaryExpr(expr);
        }

        @Override
        public void visitCallExpr(CallExpression expr) {
            maybeAddRandomReport(expr, 5);
            super.visitCallExpr(expr);
        }

        @Override
        public void visitConditionalExpr(ConditionalExpression expr) {
            maybeAddRandomReport(expr, 5);
            super.visitConditionalExpr(expr);
        }

        @Override
        public void visitPropertyAccessExpr(PropertyAccessExpression expr) {
            maybeAddRandomReport(expr, 5);
            super.visitPropertyAccessExpr(expr);
        }

        @Override
        public void visitRecordExpr(RecordExpression expr) {
            maybeAddRandomReport(expr, 5);
            super.visitRecordExpr(expr);
        }

        @Override
        public void visitSetExpr(SetExpression expr) {
            maybeAddRandomReport(expr, 5);
            super.visitSetExpr(expr);
        }

        @Override
        public void visitUnaryExpr(UnaryExpression expr) {
            maybeAddRandomReport(expr, 5);
            super.visitUnaryExpr(expr);
        }

        private void maybeAddRandomReport(PolicyFileEntry entry, int probability) {
            int p = random.nextInt(100);
            SourceLoc loc = entry.getSourceLoc();
            if (p <= probability && loc != SourceLoc.MISSING) {
                reports.add(generateRandomReport(loc));
            }
        }

        private Report generateRandomReport(SourceLoc loc) {
            int s = random.nextInt(100);
            int m = random.nextInt(100);
            int d = random.nextInt(100);

            Severity severity = s < 34 ? Severity.Info : s < 67 ? Severity.Warning : Severity.Error;
            String message = m < 34 ? "Fantastic. Great move. Well done Angus."
                    : m < 67 ? "Ugly implementation" : "Who thought this was a good idea?";
            String detail = d < 50 ? "" : "This is a very detailed report. "
                            + "Look at all of the details that are included here. "
                            + "So many details that need to be included in the report so that you can fully understand it.";

            return new Report(severity, message, detail, loc);
        }

    }

}
