package uq.pac.rsvp.policy.datalog.driver;

import com.cedarpolicy.model.exception.AuthException;
import com.google.devtools.common.options.OptionsParser;
import org.fusesource.jansi.Ansi;
import uq.pac.rsvp.policy.datalog.ast.DLProgram;
import uq.pac.rsvp.policy.datalog.translation.Request;
import uq.pac.rsvp.policy.datalog.translation.RequestAuth;
import uq.pac.rsvp.policy.datalog.translation.Translation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.fusesource.jansi.Ansi.Color.*;
import static org.fusesource.jansi.Ansi.ansi;

public class Driver {

    private static void error(String message) {
        System.err.println(colour(RED, "ERROR: ") + message);
        System.exit(1);
    }

    private static void printUsage(OptionsParser parser) {
        System.out.println(parser.describeOptions(Collections.emptyMap(), OptionsParser.HelpVerbosity.LONG));
        System.exit(2);
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

    private static String colour(Ansi.Color color, Object text) {
        return ansi().fg(color).a(text).reset().toString();
    }

    record ExpectedRequest(Request request, RequestAuth.Result expectation) {}

    public static void main(String[] args) throws IOException, AuthException, InterruptedException {
        OptionsParser parser = OptionsParser.newOptionsParser(DriverOptions.class);
        parser.parseAndExitUponError(args);
        DriverOptions options = parser.getOptions(DriverOptions.class);
        Map<String, Object> optionsMap = options.asMap();

        Path schemaFile = requiredFile(optionsMap, "schema");
        Path policyFile = requiredFile(optionsMap, "policies");
        Path entitiesFile = requiredFile(optionsMap, "entities");
        String dlDir = requiredOpt(optionsMap, "datalog-dir", String.class);

        Path authRequests = requiredFile(optionsMap, "requests");
        List<ExpectedRequest> requests = Files.readAllLines(authRequests).stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty() && !s.startsWith("#"))
                .map(s -> {
                    String [] parts = s.split("\\s+");
                    if (parts.length != 4) {
                        error("Malformed request string (expected 4 items): " + s);
                    }
                    Request req = new Request(parts[0], parts[1], parts[2]);
                    RequestAuth.Result exp = null;
                    try {
                        exp = RequestAuth.Result.valueOf(parts[3].toUpperCase());
                    } catch (IllegalArgumentException e) {
                        error("Malformed request string (invalid expected name): " + s);
                    }
                    return new ExpectedRequest(req, exp);
                })
                .toList();

        DLProgram translation = Translation.translate(schemaFile, policyFile, entitiesFile);
        RequestAuth auth = translation.execute(Path.of(dlDir));

        if (requests.isEmpty()) {
            System.out.println(colour(RED, "No requests found"));
        } else {
            System.out.println(ansi().bold().fgBlue()
                    .a("Principal\tResource\tAction\n===============================")
                    .reset());
        }

        for (ExpectedRequest req : requests) {
            RequestAuth.Result result = auth.authorize(req.request);
            Ansi.Color colour;
            String resultStr;
            if (req.expectation == result) {
                colour = GREEN;
                resultStr = "OK";
            } else {
                colour = RED;
                resultStr = "FAIL: expected %s, got %s".formatted(req.expectation, result);
            }
            resultStr = colour(colour, resultStr);
            System.out.println(colour(YELLOW, req.request) + ": " + resultStr);
        }
        System.exit(0);
    }
}
