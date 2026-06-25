/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp;

import uq.pac.rsvp.verification.FileSet;
import uq.pac.rsvp.support.reporting.Report;
import uq.pac.rsvp.support.util.Pair;
import uq.pac.rsvp.verification.ConfigurationException;
import uq.pac.rsvp.verification.Verification;
import uq.pac.rsvp.verification.VerificationResult;
import uq.pac.rsvp.verification.impact.ChangeImpact;
import uq.pac.rsvp.verification.impact.RequestSummary;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * CLI application to run analysis of Cedar policies/invariants.
 */
public class Main {

    public static void main(String[] args) {
        FileSet fileset = new FileSet();

        // Parse each command line arg as a policy ast file
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "--schema", "-s" -> {
                    i++;
                    if (i == args.length) {
                        System.err.println("Error: --schema/-s requires an argument");
                        System.exit(1);
                    }
                    Path schemaPath = Path.of(args[i]).toAbsolutePath();
                    fileset.addSchema(schemaPath);
                }
                case "--policies", "-p" -> {
                    if (i == args.length - 1) {
                        System.err.println("Error: --policies/-p requires an argument");
                        System.exit(1);
                    }

                    List<Path> versions = new ArrayList<>();

                    // If more than one, treat them as versions of the same file:
                    while (i < args.length - 1 && !args[i + 1].startsWith("-")) {
                        versions.add(Path.of(args[++i]).toAbsolutePath());
                    }

                    fileset.addPolicies(versions.toArray(new Path[0]));
                }
                case "--entities", "-e" -> {
                    i++;
                    if (i == args.length) {
                        System.err.println("Error: --entities/-e requires an argument");
                        System.exit(1);
                    }
                    Path entitiesPath = Path.of(args[i]).toAbsolutePath();
                    fileset.addEntities(entitiesPath);
                }
                case "--invariants", "-i" -> {
                    i++;
                    if (i == args.length) {
                        System.err.println("Error: --invariants/-i requires an argument");
                        System.exit(1);
                    }
                    Path invariantsPath = Path.of(args[i]).toAbsolutePath();
                    fileset.addInvariants(invariantsPath);
                }
            }
        }

        try {
            VerificationResult result = Verification.verifyPolicies(fileset);
            Set<Report> reports = result.reports();
            for (Report report : reports) {
                System.out.println(report.toString());
            }

            for (Pair<String, String> pair : fileset.getVersionPairs()) {
                ChangeImpact impact = Verification.getImpact(pair.getKey(), pair.getValue(), result.cache());

                System.out.println("\nChange impact: " + pair.getKey() + " -> " + pair.getValue());
                System.out.println("-----------------------");

                for (RequestSummary summary : impact.forbidden()) {
                    System.out.printf(" - %s%n", summary.summary());
                }

                for (RequestSummary summary : impact.permitted()) {
                    System.out.printf(" + %s%n", summary.summary());
                }
            }

        } catch (IOException io) {
            System.err.println("IO Error");
            io.printStackTrace();
            System.exit(1);
        } catch (InterruptedException ie) {
            System.err.println("Interrupted.");
            System.exit(1);
        } catch (ConfigurationException ce) {
            System.err.println("Configuration issue: " + ce.getMessage());
            System.exit(1);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
