package uq.pac.rsvp.support.reporting;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import uq.pac.rsvp.support.SourceLoc;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

public class Report {

    public static enum Severity {

        @SerializedName("info")
        Info("info"),

        @SerializedName("warn")
        Warning("warn"),

        @SerializedName("err")
        Error("err");

        @JsonValue
        final String name;

        Severity(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private final HashCode id;
    private final Severity severity;
    private final String message;
    private final String detail;
    private final SourceLoc primary;
    private final Set<SourceLoc> locations;

    public Report(Severity severity, String message, String detail, SourceLoc primary, SourceLoc... locations) {
        this.severity = severity;
        this.message = message;
        this.detail = detail;
        this.primary = primary;
        this.locations = Set.of(locations);
        this.id = Hashing.sha256().hashString(
                severity.name + message + detail + primary.toString() + this.locations.toString(),
                StandardCharsets.UTF_8);
    }

    public Report(Severity severity, String message, SourceLoc primary, SourceLoc... locations) {
        this(severity, message, "", primary, locations);
    }

    public String getId() {
        return id.toString();
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageDetail() {
        return detail;
    }

    public SourceLoc getPrimarySourceLocation() {
        return primary;
    }

    public Set<SourceLoc> getSourceLocations() {
        return Set.copyOf(locations);
    }

    @Override
    public String toString() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }

    @Override
    public int hashCode() {
        return id.asInt();
    }

    /**
     * Create a new report identical to this one but with all source locations
     * remapped to files based on the supplied mapping.
     *
     * @param filenames the mapping of original -> remapped source file names
     * @return a new {@code Report} with all source locations referencing the
     *         supplied file names or {@code SourceLoc.MISSING}
     */
    public Report remap(Map<String, String> filenames) {

        SourceLoc[] additionalLocations = new SourceLoc[locations.size()];

        int i = 0;
        for (SourceLoc loc : locations) {
            additionalLocations[i++] = loc.cloneForFile(filenames.get(loc.file));
        }

        return new Report(severity, message, detail, primary.cloneForFile(filenames.get(primary.file)), additionalLocations);
    }
}
