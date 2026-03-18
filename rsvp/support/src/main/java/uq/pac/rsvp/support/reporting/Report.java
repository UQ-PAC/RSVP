package uq.pac.rsvp.support.reporting;

import java.nio.charset.StandardCharsets;

import com.google.common.hash.Hashing;
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

    private final String id;
    private final Severity severity;
    private final String message;
    private final SourceLoc source;

    public Report(Severity severity, String message, SourceLoc source) {
        this.id = Hashing.sha256().hashString(severity.name + message + source.toString(), StandardCharsets.UTF_8)
                .toString();
        this.severity = severity;
        this.message = message;
        this.source = source;
    }

    public String getId() {
        return id;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }

    public SourceLoc getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "{ id: " + id + ", severity: " + severity.getName() + ", message: " + message + ", source: "
                + source.toString() + " }";
    }
}
