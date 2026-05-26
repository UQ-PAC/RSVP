package uq.pac.rsvp.policy.datalog.driver;

import com.cedarpolicy.AuthorizationEngine;
import com.cedarpolicy.BasicAuthorizationEngine;
import com.cedarpolicy.model.AuthorizationRequest;
import com.cedarpolicy.model.AuthorizationResponse;
import com.cedarpolicy.model.AuthorizationSuccessResponse;
import com.cedarpolicy.model.entity.Entities;
import com.cedarpolicy.model.exception.AuthException;
import com.cedarpolicy.model.policy.PolicySet;
import com.cedarpolicy.value.*;
import com.google.devtools.common.options.OptionsParser;
import com.google.gson.FormattingStyle;
import com.google.gson.stream.JsonWriter;
import uq.pac.rsvp.StdLogger;
import uq.pac.rsvp.policy.ast.entity.*;
import uq.pac.rsvp.policy.ast.policy.Policy;
import uq.pac.rsvp.policy.ast.policy.PolicyProgram;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.datalog.translation.Request;
import uq.pac.rsvp.policy.datalog.translation.RequestAuth;
import uq.pac.rsvp.policy.datalog.translation.Translation;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static org.fusesource.jansi.Ansi.Color.BLUE;
import static org.fusesource.jansi.Ansi.Color.DEFAULT;
import static org.fusesource.jansi.Ansi.Color.GREEN;
import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.Color.YELLOW;
import static uq.pac.rsvp.Assertion.require;

public class Driver {

    private static final StdLogger logger = new StdLogger();

    private static void error(String message) {
        logger.error(message);
        System.exit(1);
    }

    private static <E> E requiredOpt(Map<String, Object> options, String option, Class<E> cls) {
        Object optionValue = options.get(option);
        if (optionValue == null) {
            error("Required option: '%s' has not been provided".formatted(option));
        } else {
            return cls.cast(optionValue);
        }
        return null;
    }

    private static Path requiredFile(Map<String, Object> options, String option) {
        String filename = requiredOpt(options, option, String.class);
        if (filename != null && !filename.isEmpty()) {
            Path path = Path.of(filename);
            if (Files.exists(path) && Files.isRegularFile(path)) {
                return path;
            } else {
                error("File '%s' (option %s) does not exist or not a regular file".formatted(filename, option));
            }
        } else {
            error("Required option: '%s' has not been provided".formatted(option));
        }
        return null;
    }

    private static void validate(RequestAuth rsvpAuth, Schema schema, PolicyProgram program, EntitySet entities) throws IOException, AuthException {
        AuthorizationEngine cedarAuth = new BasicAuthorizationEngine();

        Entities cedarEntities = Entities.parse(entities.toJson().toString());
        com.cedarpolicy.model.schema.Schema cedarSchema =
                com.cedarpolicy.model.schema.Schema.parse(
                        com.cedarpolicy.model.schema.Schema.JsonOrCedar.Cedar, schema.toString());
        PolicySet cedarPolicies = PolicySet.parsePolicies(
                program.policies().map(Policy::toString).collect(Collectors.joining("\n")));

        int[] rsvpRequestCounter = new int[2];
        int[] cedarRequestCounter = new int[2];
        for (Request rsvpRequest : rsvpAuth.getActionableRequests()) {
            RequestAuth.Decision rsvpDecision = rsvpAuth.authorize(rsvpRequest);
            require(rsvpDecision == RequestAuth.Decision.Deny ||
                    rsvpDecision == RequestAuth.Decision.Allow);

            AuthorizationRequest cedarRequest = rsvpRequest.getCedarRequest(cedarSchema);
            AuthorizationResponse cedarResponse =
                    cedarAuth.isAuthorized(cedarRequest, cedarPolicies, cedarEntities);

            if (!cedarResponse.type.equals(AuthorizationResponse.SuccessOrFailure.Success)) {
                error(cedarResponse.errors.orElseThrow(AssertionError::new).toString());
            }

            AuthorizationSuccessResponse cedarSuccess =
                    cedarResponse.success.orElseThrow(AssertionError::new);
            AuthorizationSuccessResponse.Decision cedarDecision = cedarSuccess.getDecision();

            if (cedarDecision == AuthorizationSuccessResponse.Decision.Allow &&
                    rsvpDecision == RequestAuth.Decision.Allow) {
                logger.info(GREEN, rsvpRequest + ":  " + rsvpDecision);
            } else if (cedarDecision == AuthorizationSuccessResponse.Decision.Deny &&
                    rsvpDecision == RequestAuth.Decision.Deny) {
                logger.info(RED, rsvpRequest + ":  " + rsvpDecision);
            } else {
                logger.error(rsvpRequest + ":  RSVP: " + rsvpDecision + " / Cedar:" + cedarDecision);
            }
            cedarRequestCounter[cedarDecision.ordinal()]++;
            rsvpRequestCounter[rsvpDecision.ordinal()]++;
        }
        logger.bold().info(GREEN, "RSVP Requests (allow/deny): %d/%d\n",
                rsvpAuth.getActionableRequests().size(), rsvpRequestCounter[0], rsvpRequestCounter[1]);
        logger.bold().info(BLUE, "Cedar Requests (allow/deny): %d/%d\n",
                rsvpAuth.getActionableRequests().size(), cedarRequestCounter[0], cedarRequestCounter[1]);

        if (!Arrays.equals(rsvpRequestCounter, cedarRequestCounter)) {
            error("Validation failed");
        } else {
            logger.bold().info(GREEN, "All requests validated");
        }
    }

    private static void writeRequests(JsonWriter writer, Collection<Request> requests, String name) throws IOException {
        writer.name(name);
        writer.beginArray();
        for (Request r : requests) {
            writer.beginObject();
            writer.name("principal");
            writer.value(r.getPrincipal());
            writer.name("resource");
            writer.value(r.getResource());
            writer.name("action");
            writer.value(r.getAction());
            writer.endObject();
        }
        writer.endArray();
    }

    public static void writeRequests(Path file, RequestAuth auth) throws IOException {
        JsonWriter writer = new JsonWriter(new FileWriter(file.toFile()));
        writer.setFormattingStyle(FormattingStyle.PRETTY);
        writer.beginObject();
        writeRequests(writer, auth.getPermittedRequests().getRequests(), "permitted");
        writeRequests(writer, auth.getForbiddenRequests().getRequests(), "forbidden");
        writer.endObject();
        writer.close();
    }

    public static void main(String[] args) throws IOException, AuthException, IllegalAccessException {
        OptionsParser parser = OptionsParser.newOptionsParser(DriverOptions.class);
        parser.parseAndExitUponError(args);
        DriverOptions options = parser.getOptions(DriverOptions.class);
        Map<String, Object> optionsMap = options.asMap();

        if (options.help) {
            logger.info(DEFAULT, parser.describeOptions(Collections.emptyMap(), OptionsParser.HelpVerbosity.LONG));
            System.exit(2);
        }

        Path schemaPath = requiredFile(optionsMap, "schema");
        Path programPath = requiredFile(optionsMap, "policies");
        Path entityPath = requiredFile(optionsMap, "entities");
        String dlDir = requiredOpt(optionsMap, "datalog-dir", String.class);
        Path dlPath = Path.of(dlDir);

        Schema schema = Schema.parse(schemaPath);
        PolicyProgram program = PolicyProgram.parse(programPath);
        EntitySet entities = EntitySet.parse(entityPath);

        if (Files.exists(dlPath) && !Files.isDirectory(dlPath)) {
            error("Datalog destination: " + dlPath + " is not a directory");
        }

        Translation translation = new Translation(schema, program, entities, dlPath);

        RequestAuth rsvpAuth = new RequestAuth(translation);
        logger.info(YELLOW, "Datalog output written to directory: " + dlPath.toAbsolutePath());
        writeRequests(Path.of(dlPath.toString(), "auth.json"), rsvpAuth);

        if (options.validate) {
            validate(rsvpAuth, schema, program, entities);
        }

        System.exit(0);
    }
}
