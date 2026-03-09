package uq.pac.rsvp.support.reporting;

import com.google.gson.annotations.SerializedName;

import uq.pac.rsvp.support.SourceLoc;

public class Report {

    public static enum Severity {

        @SerializedName("info")
        Info("info"),

        @SerializedName("warn")
        Warning("warn"),

        @SerializedName("err")
        Error("err");

        final String name;

        Severity(String name) {
            this.name = name;
        }
    }

    private final Severity severity;
    private final String message;
    private final SourceLoc source;

    public Report(Severity severity, String message, SourceLoc source) {
        this.severity = severity;
        this.message = message;
        this.source = source;
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
}
