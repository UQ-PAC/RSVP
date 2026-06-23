/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.verification;

import uq.pac.rsvp.policy.ast.AstNode;
import uq.pac.rsvp.policy.ast.entity.Entity;
import uq.pac.rsvp.policy.ast.entity.EntityReference;
import uq.pac.rsvp.policy.ast.entity.EntityValue;
import uq.pac.rsvp.policy.ast.entity.RecordValue;
import uq.pac.rsvp.policy.ast.policy.Invariant;
import uq.pac.rsvp.policy.ast.policy.Policy;
import uq.pac.rsvp.policy.ast.policy.PolicyProgram;
import uq.pac.rsvp.policy.ast.policy.expr.*;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyVisitorAdapter;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.statement.ActionDefinition;
import uq.pac.rsvp.policy.ast.schema.statement.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.statement.EnumEntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.statement.RecordEntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.type.BooleanType;
import uq.pac.rsvp.policy.ast.schema.type.DateTimeType;
import uq.pac.rsvp.policy.ast.schema.type.DecimalType;
import uq.pac.rsvp.policy.ast.schema.type.DurationType;
import uq.pac.rsvp.policy.ast.schema.type.IpAddressType;
import uq.pac.rsvp.policy.ast.schema.type.LongType;
import uq.pac.rsvp.policy.ast.schema.type.RecordType;
import uq.pac.rsvp.policy.ast.schema.type.SetType;
import uq.pac.rsvp.policy.ast.schema.type.StringType;
import uq.pac.rsvp.policy.ast.schema.type.TypeReference;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaVisitorAdapter;
import uq.pac.rsvp.support.SourceLoc;
import uq.pac.rsvp.support.reporting.Report;
import uq.pac.rsvp.verification.policy.PolicyReport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RandomReportGenerator {
    private final Random random;
    private final Set<SourceLoc> additionalLocations;

    public static Set<Report> generateRandomReports(FileSet fileset) throws IOException, IllegalAccessException {
        Set<Report> results = new HashSet<>();

        fileset.loadFiles();

        Schema schema = Schema.of(fileset.getSchemaStatements());
        PolicyProgram policyProgram = fileset.getPolicyProgram();
        Collection<Policy> policyAst = policyProgram.getPolicies();
        Collection<Invariant> invariantAst = policyProgram.getInvariants();
        Set<Entity> entitySet = fileset.getEntities();

        RandomReportGenerator randomGenerator = new RandomReportGenerator();
        RandomReportGenerator.RandomEntityReportGenerator entityReportGenerator = new RandomEntityReportGenerator(randomGenerator);
        RandomReportGenerator.RandomPolicyReportGenerator policyReportGenerator = new RandomPolicyReportGenerator(randomGenerator);
        RandomReportGenerator.RandomSchemaReportGenerator schemaReportGenerator = new RandomSchemaReportGenerator(randomGenerator);

        invariantAst.forEach(invariant -> {
            int i = randomGenerator.nextRandomNumber();

            if (i < 20) {
                results.add(randomGenerator.generateRandomInvariantReport(invariant));
            }
        });

        schema.statements().forEach(s -> s.accept(schemaReportGenerator));
        policyAst.forEach(policy -> policy.accept(policyReportGenerator));
        invariantAst.forEach(invariant -> invariant.accept(policyReportGenerator));
        entitySet.forEach(entity -> entityReportGenerator.maybeAddRandomReports(entity, 20));

        results.add(randomGenerator.generateRandomReport(null, "Mystery"));
        results.add(randomGenerator.generateRandomReport(null, "Mystery"));
        results.add(randomGenerator.generateRandomReport(null, "Mystery"));

        results.addAll(schemaReportGenerator.reports);
        results.addAll(policyReportGenerator.reports);
        results.addAll(entityReportGenerator.reports);

        return results;
    }

    public RandomReportGenerator() {
        random = new Random();
        additionalLocations = new HashSet<>();
    }

    private static class RandomSchemaReportGenerator extends SchemaVisitorAdapter {

        private final Set<Report> reports;
        private final RandomReportGenerator generator;

        private RandomSchemaReportGenerator(RandomReportGenerator generator) {
            reports = new HashSet<>();
            this.generator = generator;
        }

        @Override
        public void visitRecordEntity(RecordEntityTypeDefinition entity) {
            maybeAddRandomReport(entity, 30);
            super.visitRecordEntity(entity);
        }

        @Override
        public void visitEnumEntity(EnumEntityTypeDefinition entity) {
            maybeAddRandomReport(entity, 30);
        }

        @Override
        public void visitAction(ActionDefinition action) {
            maybeAddRandomReport(action, 30);
            super.visitAction(action);
        }

        @Override
        public void visitCommon(CommonTypeDefinition type) {
            maybeAddRandomReport(type, 30);
            super.visitCommon(type);
        }

        @Override
        public void visitRecord(RecordType type) {
            maybeAddRandomReport(type, 20);
            super.visitRecord(type);
        }

        @Override
        public void visitSet(SetType type) {
            maybeAddRandomReport(type, 5);
            super.visitSet(type);
        }

        @Override
        public void visitTypeReference(TypeReference type) {
            maybeAddRandomReport(type, 5);
        }

        @Override
        public void visitBoolean(BooleanType type) {
            maybeAddRandomReport(type, 5);
        }

        @Override
        public void visitLong(LongType type) {
            maybeAddRandomReport(type, 5);
        }

        @Override
        public void visitString(StringType type) {
            maybeAddRandomReport(type, 5);
        }

        @Override
        public void visitIpAddress(IpAddressType type) {
            maybeAddRandomReport(type, 5);
        }

        @Override
        public void visitDecimal(DecimalType type) {
            maybeAddRandomReport(type, 5);
        }

        @Override
        public void visitDateTime(DateTimeType type) {
            maybeAddRandomReport(type, 5);
        }

        @Override
        public void visitDuration(DurationType type) {
            maybeAddRandomReport(type, 5);
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

    private static class RandomPolicyReportGenerator extends PolicyVisitorAdapter {

        private final Set<Report> reports;
        private final RandomReportGenerator generator;

        private Policy secondPolicy = null;

        private RandomPolicyReportGenerator(RandomReportGenerator generator) {
            reports = new HashSet<>();
            this.generator = generator;
        }

        @Override
        public void visitPolicy(Policy policy) {
            int p = generator.nextRandomNumber();

            if (p < 50) {
//                maybeAddRandomReport(policy, 30);
//            } else if (p < 80) {
                reports.add(generator.generateRandomPolicyReport(policy, secondPolicy));
            } else if (p < 75) {
                secondPolicy = policy;
            } else {
                secondPolicy = null;
            }
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

        @Override
        public void visitHasExpr(HasExpression expr) {
            maybeAddRandomReport(expr, 5);
            super.visitHasExpr(expr);
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

    private static class RandomEntityReportGenerator {
        private final Set<Report> reports;
        private final RandomReportGenerator generator;

        private RandomEntityReportGenerator(RandomReportGenerator generator) {
            reports = new HashSet<>();
            this.generator = generator;
        }

        private void maybeAddRandomReports(Entity entity, int probability) {
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

    private Report generateRandomPolicyReport(Policy p1, Policy p2) {
        int r = nextRandomNumber();
        if (p2 != null) {
            if (r < 50) {
                return new PolicyReport.IdenticalPolicies(p1, p2);
            } else {
                return new PolicyReport.SubsumedPolicy(p1, p2);
            }
        } else {
            if (r < 50) {
                return new PolicyReport.UnusedPolicy(p1);
            } else {
                return new PolicyReport.SubsumedPolicy(p1);
            }
        }
    }

    private Report generateRandomInvariantReport(Invariant i) {
        return new PolicyReport.InvariantNotHeld(i, "Counterexamples");
    }

    private Report generateRandomReport(SourceLoc loc, String entryType) {
        int s = random.nextInt(100);
        int m = random.nextInt(100);
        int d = random.nextInt(100);
        int l = random.nextInt(100);

        Report.Severity severity = s < 34 ? Report.Severity.Info : s < 67 ? Report.Severity.Warning : Report.Severity.Error;
        String message = m < 34 ? "Fantastic. Great move. Well done Angus." : m < 67 ? "Ugly implementation" : "Who thought this was a good idea?";
        String detail = d < 50 ? "" : "This is a very detailed report. " + "Look at all of the details that are included here. " + "So many details that need to be included in the report so that you can fully understand it.";

        List<Report.LocationMessage> locations = new ArrayList<>();

        if (loc != null) {
            locations.add(new Report.LocationMessage(loc, "Prime location"));

            if (!additionalLocations.isEmpty() && l < 25) {
                for (SourceLoc additionalLocation : additionalLocations) {
                    int e = random.nextInt(100);
                    locations.add(new Report.LocationMessage(additionalLocation, e < 34 ? "This is a problematic location" : e < 67 ? "Spectacular location" : ""));
                }
                additionalLocations.clear();
            }
        }

        return new Report(severity, "(" + entryType + "): " + message, detail, locations.toArray(new Report.LocationMessage[0]));
    }

    private void addAdditionalLocation(SourceLoc loc) {
        additionalLocations.add(loc);
    }

    private int nextRandomNumber() {
        return random.nextInt(100);
    }
}
