package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.datalog.ast.DLRuleDecl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static uq.pac.rsvp.policy.datalog.translation.RequestAuth.Result.*;
import static uq.pac.rsvp.policy.datalog.util.Assertion.require;

public class RequestAuth {

    private final Set<Request> permitted;
    private final Set<Request> forbidden;
    private final Set<Request> universe;
    private final Set<Request> actionable;

    private RequestAuth(Set<Request> universe, Set<Request> actionable,
                        Set<Request> permitted, Set<Request> forbidden) {
        this.universe = universe;
        this.permitted = permitted;
        this.forbidden = forbidden;
        this.actionable = actionable;

        // Permitted and forbidden are disjoint sets
        permitted.forEach(s -> require(!forbidden.contains(s)));
        // Permitted and forbidden sets should be in the universe of potential requests
        require(universe.containsAll(permitted));
        require(universe.containsAll(forbidden));
    }

    public enum Result {
        PERMITTED,
        FORBIDDEN,
        INVALID
    }

    public Result authorize(Request request) {
        if (permitted.contains(request)) {
            return PERMITTED;
        } else if (forbidden.contains(request)) {
            return FORBIDDEN;
        } else if (universe.contains(request)) {
            return FORBIDDEN;
        }
        return INVALID;
    }

    private static Set<Request> readFile(Path file) {
        BufferedReader reader;
        Set<Request> set = new HashSet<>();
        try {
            reader = new BufferedReader(new FileReader(file.toFile()));
            String line = reader.readLine();
            while (line != null) {
                set.add(new Request(line.trim()));
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            throw new TranslationError(e.getMessage());
        }
        return set;
    }

    private static Set<Request> loadRequests(Path dir, DLRuleDecl decl) {
        Path csv = Path.of(dir.toString(), decl.getName() + ".csv");
        if (Files.exists(csv)) {
            throw new TranslationError("Target CSV file %s does not exist or not a directory".formatted(csv));
        }
        return readFile(csv);
    }

    public static RequestAuth load(Path dir) {
        if (!Files.exists(dir) ||  !Files.isDirectory(dir)) {
            throw new TranslationError("Target directory %s does not exist or not a directory".formatted(dir));
        }
        Set<Request> universe = loadRequests(dir, TranslationConstants.AllRequestsRuleDecl);
        Set<Request> actionable = loadRequests(dir, TranslationConstants.AllActionableRequestsRuleDecl);
        Set<Request> permitted = loadRequests(dir, TranslationConstants.PermittedRequestsRuleDecl);
        Set<Request> forbidden = loadRequests(dir, TranslationConstants.ForbiddenRequestsRuleDecl);
        return new RequestAuth(universe, actionable, permitted, forbidden);
    }

    public Set<Request> getAllActionableRequests() {
        return actionable;
    }

    public Set<Request> getAllPermittedRequests() {
        return permitted;
    }

    public Set<Request> getAllForbiddenRequests() {
        return forbidden;
    }

    public Set<Request> getRequestUniverse() {
        return universe;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Permitted: \n");
        for (Request p : permitted) {
            sb.append("   ").append(p.toString()).append('\n');
        }
        return sb.toString();
    }
}
