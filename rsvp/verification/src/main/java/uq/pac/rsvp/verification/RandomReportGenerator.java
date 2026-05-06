package uq.pac.rsvp.verification;

import uq.pac.rsvp.policy.ast.AstNode;
import uq.pac.rsvp.policy.ast.entity.Entity;
import uq.pac.rsvp.policy.ast.entity.EntityReference;
import uq.pac.rsvp.policy.ast.entity.EntityValue;
import uq.pac.rsvp.policy.ast.entity.RecordValue;
import uq.pac.rsvp.policy.ast.policy.Policy;
import uq.pac.rsvp.policy.ast.policy.expr.*;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyVisitorImpl;
import uq.pac.rsvp.support.SourceLoc;
import uq.pac.rsvp.support.reporting.Report;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class RandomReportGenerator {
    private final Random random;
    private final Set<SourceLoc> additionalLocations;

    public RandomReportGenerator() {
        random = new Random();
        additionalLocations = new HashSet<>();
    }

    public static class RandomPolicyReportGenerator extends PolicyVisitorImpl {

        public final Set<Report> reports;
        private final RandomReportGenerator generator;

        public RandomPolicyReportGenerator(RandomReportGenerator generator) {
            reports = new HashSet<>();
            this.generator = generator;
        }

        @Override
        public void visitPolicy(Policy policy) {
            maybeAddRandomReport(policy, 50);
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

        private void maybeAddRandomReport(AstNode entry, int probability) {
            int p = generator.nextRandomNumber();
            SourceLoc loc = entry.getSourceLoc();

            if (!loc.isEmpty()) {
                if (p <= probability) {
                    String[] name = entry.getClass().getName().split("\\.");
                    reports.add(generator.generateRandomReport(loc, name[name.length - 1]));
                } else if (p <= probability * 2) {
                    generator.addAdditionalLocation(loc);
                }
            }
        }
    }

    public static class RandomEntityReportGenerator {
        public final Set<Report> reports;
        private final RandomReportGenerator generator;

        public RandomEntityReportGenerator(RandomReportGenerator generator) {
            reports = new HashSet<>();
            this.generator = generator;
        }

        public void maybeAddRandomReports(Entity entity, int probability) {
            int p = generator.nextRandomNumber();
            SourceLoc loc = entity.getSourceLoc();

            if (!loc.isEmpty()) {
                if (p <= probability) {
                    reports.add(generator.generateRandomReport(loc, "Entity"));
                } else if (p <= probability * 2) {
                    generator.addAdditionalLocation(loc);
                }
            }

            generateRandomReferenceReports(entity.getEuid(), 10);
            entity.getParents().forEach(parent -> generateRandomReferenceReports(parent, 10));

            generateRandomRecordValueReports(entity.getAttrs(), 5);

            entity.getAttrs().forEach((name, value) -> generateRandomEntityValueReports(value, 10));
        }

        private void generateRandomReferenceReports(EntityReference ref, int probability) {
            int p = generator.nextRandomNumber();
            SourceLoc loc = ref.getSourceLoc();

            if (!loc.isEmpty()) {
                if (p <= probability) {
                    reports.add(generator.generateRandomReport(loc, "EntityReference"));
                } else if (p <= probability * 2) {
                    generator.addAdditionalLocation(loc);
                }
            }
        }

        private void generateRandomRecordValueReports(RecordValue value, int probability) {
            int p = generator.nextRandomNumber();
            SourceLoc loc = value.getSourceLoc();

            if (!loc.isEmpty()) {
                if (p <= probability) {
                    reports.add(generator.generateRandomReport(loc, "RecordValue"));
                } else if (p <= probability * 2) {
                    generator.addAdditionalLocation(loc);
                }
            }
        }

        private void generateRandomEntityValueReports(EntityValue value, int probability) {
            int p = generator.nextRandomNumber();
            SourceLoc loc = value.getSourceLoc();

            if (!loc.isEmpty()) {
                if (p <= probability) {
                    reports.add(generator.generateRandomReport(value.getSourceLoc(), "EntityValue"));
                } else if (p <= probability * 2) {
                    generator.addAdditionalLocation(value.getSourceLoc());
                }
            }
        }
    }

    private Report generateRandomReport(SourceLoc loc, String entryType) {
        int s = random.nextInt(100);
        int m = random.nextInt(100);
        int d = random.nextInt(100);
        int l = random.nextInt(100);

        Report.Severity severity = s < 34 ? Report.Severity.Info : s < 67 ? Report.Severity.Warning : Report.Severity.Error;
        String message = m < 34 ? "Fantastic. Great move. Well done Angus." : m < 67 ? "Ugly implementation" : "Who thought this was a good idea?";
        String detail = d < 50 ? "" : "This is a very detailed report. " + "Look at all of the details that are included here. " + "So many details that need to be included in the report so that you can fully understand it.";

        SourceLoc[] additional = new SourceLoc[0];

        if (l < 25) {
            additional = additionalLocations.toArray(new SourceLoc[0]);
            additionalLocations.clear();
        }

        return new Report(severity, "(" + entryType + "): " + message, detail, loc, additional);
    }

    private void addAdditionalLocation(SourceLoc loc) {
        additionalLocations.add(loc);
    }

    private int nextRandomNumber() {
        return random.nextInt(100);
    }
}
