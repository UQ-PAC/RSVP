package uq.pac.rsvp.policy.datalog.util;

import org.fusesource.jansi.Ansi;

import java.io.PrintStream;

import static org.fusesource.jansi.Ansi.Color.*;
import static org.fusesource.jansi.Ansi.ansi;

/**
 * Very simple logger for standard streams
 */
public class Logger {

    public enum Level {
        Severe("ERROR"),
        Warning("WARNING"),
        Info(""),
        Config(""),
        Fine(""),
        Finer(""),
        Finest("");

        private final String desc;

        Level(String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return desc;
        }

        public String desc() {
            return desc;
        }
    }

    private Level level = Level.Info;

    public void setLevel(Level level) {
        this.level = level;
    }

    synchronized private Logger log(PrintStream stream, Ansi.Color color, Level level, String format, Object ...args) {
        if (level.ordinal() <= this.level.ordinal()) {
            String outs = String.format(format, args);
            if (!level.desc.isEmpty()) {
                outs = level.desc + ": " + outs;
            }
            for (String formatted : outs.split("\n")) {
                stream.println(ansi().fg(color).a(formatted).reset());
            }
        }
        return this;
    }

    public Logger info(Ansi.Color color, String format, Object ...args) {
        return log(System.out, color, Level.Info, format, args);
    }

    public Logger info(String format, Object ...args) {
        return log(System.out, DEFAULT, Level.Info, format, args);
    }

    public Logger warning(String format, Object ...args) {
        return log(System.out, RED, Level.Warning, format, args);
    }

    public Logger error(String format, Object ...args) {
        return log(System.out, RED, Level.Severe, format, args);
    }
}
