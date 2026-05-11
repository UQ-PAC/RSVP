package uq.pac.rsvp.support.reporting;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import uq.pac.rsvp.support.SourceLoc;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

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

    public static class LocationMessage {
        private final SourceLoc location;
        private final String message;

        public LocationMessage(SourceLoc location, String message) {
            this.location = location;
            this.message = message;
        }

        public SourceLoc getLocation() {
            return location;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "%s: %s".formatted(location.toString(), message);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            } else if (obj == this) {
                return true;
            }

            if (!(obj instanceof LocationMessage other)) {
                return false;
            }

            return message.equals(other.message) && !location.equals(other.location);
        }
    }

    private final HashCode id;
    private final Severity severity;
    private final String message;
    private final String detail;
    private final List<LocationMessage> locations;

    public Report(Severity severity, String message, String detail, LocationMessage... locations) {
        this.severity = severity;
        this.message = message;
        this.detail = detail;
        this.locations = locations == null ? Collections.emptyList() : List.of(locations);
        this.id = Hashing.sha256().hashString(
                severity.name + message + detail + this.locations,
                StandardCharsets.UTF_8);

    }

    public Report(Severity severity, String message, LocationMessage... locations) {
        this(severity, message, "", locations);
    }

    public Report(Severity severity, String message, String detail, SourceLoc location) {
        this(severity, message, detail, new LocationMessage(location, ""));
    }

    public Report(Severity severity, String message, SourceLoc location) {
        this(severity, message, new LocationMessage(location, ""));

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

    public List<LocationMessage> getSourceLocations() {
        return List.copyOf(locations);
    }

    @Override
    public String toString() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }

    @Override
    public int hashCode() {
        return id.asInt();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        }

        if (!(obj instanceof Report other)) {
            return false;
        }

        return severity.equals(other.severity) && message.equals(other.message) && detail.equals(other.detail) && equalsLocations(other.locations);
    }

    private boolean equalsLocations(List<LocationMessage> other) {
        if (locations.size() != other.size()) {
            return false;
        }

        for (int i = 0; i < locations.size(); i++) {
            if (!locations.get(i).equals(other.get(i))) {
                return false;
            }
        }

        return true;
    }
}
