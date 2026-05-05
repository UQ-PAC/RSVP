package uq.pac.rsvp;

import uq.pac.rsvp.support.reporting.Report;
import uq.pac.rsvp.verification.ConfigurationException;
import uq.pac.rsvp.verification.Verification;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * CLI application to run analysis of Cedar policies/invariants.
 */
public class Main {

    public static void main(String[] args) throws IOException {

        class PolicyFileSet {
            Set<Path> schemaPaths = new HashSet<>();
            Set<List<Path>> policiesPaths = new HashSet<>();
            Set<Path> entitiesPaths = new HashSet<>();
            Set<Path> invariantsPaths = new HashSet<>();
        }

        PolicyFileSet fileSet = new PolicyFileSet();

        // Parse each command line arg as a policy ast file
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--schema") || arg.equals("-s")) {
                i++;
                if (i == args.length) {
                    System.err.println("Error: --schema/-s requires an argument");
                    System.exit(1);
                }
                Path schemaPath = Path.of(args[i]).toAbsolutePath();
                fileSet.schemaPaths.add(schemaPath);
            } else if (arg.equals("--policies") || arg.equals("-p")) {
                i++;
                if (i == args.length) {
                    System.err.println("Error: --policies/-p requires an argument");
                    System.exit(1);
                }
                Path policiesPath = Path.of(args[i]).toAbsolutePath();
                fileSet.policiesPaths.add(List.of(policiesPath));
            } else if (arg.equals("--entities") || arg.equals("-e")) {
                i++;
                if (i == args.length) {
                    System.err.println("Error: --entities/-e requires an argument");
                    System.exit(1);
                }
                Path entitiesPath = Path.of(args[i]).toAbsolutePath();
                fileSet.entitiesPaths.add(entitiesPath);
            } else if (arg.equals("--invariants") || arg.equals("-i")) {
                i++;
                if (i == args.length) {
                    System.err.println("Error: --invariants/-i requires an argument");
                    System.exit(1);
                }
                Path invariantsPath = Path.of(args[i]).toAbsolutePath();
                fileSet.entitiesPaths.add(invariantsPath);
            }

            try {
                Set<Report> reports = Verification.verifyPolicies(fileSet.policiesPaths,
                        fileSet.schemaPaths, fileSet.entitiesPaths, fileSet.invariantsPaths);
                for (Report report : reports) {
                    System.out.println(report.toString());
                }
            } catch (InterruptedException ie) {
                System.err.println("Interrupted.");
                System.exit(1);
            } catch (ConfigurationException ce) {
                System.err.println("Configuration issue: " + ce.getMessage());
                System.exit(1);
            } catch (RsvpException | IllegalAccessException rsvpe) {
                rsvpe.printStackTrace();
                System.exit(1);
            }
        }
    }
}
