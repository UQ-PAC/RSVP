package uq.pac.rsvp.policy.datalog.translation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static uq.pac.rsvp.policy.datalog.translation.RequestAuth.Result.INVALID;
import static uq.pac.rsvp.policy.datalog.util.Assertion.require;

public class RequestAuth {

    public final Set<Request> permitted;
    public final Set<Request> forbidden;
    public final Set<Request> universe;

    private RequestAuth(Set<Request> universe,
                        Set<Request> permitted, Set<Request> forbidden) {
        this.universe = universe;
        this.permitted = permitted;
        this.forbidden = forbidden;

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
            return Result.PERMITTED;
        } else if (forbidden.contains(request)) {
            return Result.FORBIDDEN;
        } else if (universe.contains(request)) {
            return Result.FORBIDDEN;
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

    public static RequestAuth load(Path dir) {
        if (!Files.exists(dir) ||  !Files.isDirectory(dir)) {
            throw new TranslationError("Target directory %s does not exist or not a directory".formatted(dir));
        }

        Path universeFile = Path.of(dir.toString(),
                TranslationConstants.AllRequestsRuleDecl.getName() + ".csv");
        Set<Request> universe = readFile(universeFile);

        Path permittedFile = Path.of(dir.toString(),
                TranslationConstants.PermittedRequestsRuleDecl.getName() + ".csv");
        Set<Request> permitted = readFile(permittedFile);

        Path forbiddenFile = Path.of(dir.toString(),
                TranslationConstants.ForbiddenRequestsRuleDecl.getName() + ".csv");
        Set<Request> forbidden = readFile(forbiddenFile);
        return new RequestAuth(universe, permitted, forbidden);
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
