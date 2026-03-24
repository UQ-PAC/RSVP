package uq.pac.rsvp.support.reporting;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import com.fasterxml.jackson.annotation.JsonValue;

import uq.pac.rsvp.support.SourceLoc;

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

    public String getDetailMessage() {
        return detail;
    }

    public SourceLoc getPrimarySourceLocation() {
        return primary;
    }

    public Set<SourceLoc> getNonPrimarySourceLocations() {
        return Set.copyOf(locations);
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    @Override
    public int hashCode() {
        return id.asInt();
    }
}
