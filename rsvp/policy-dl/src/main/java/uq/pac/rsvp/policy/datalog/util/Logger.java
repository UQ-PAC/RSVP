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

    private Ansi console;

    public Logger () {
        this.console = ansi();
    }

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
                console.fg(color).a(formatted).newline();
            }
            console.reset();
            stream.print(console);
            console = ansi();
        }
        return this;
    }

    synchronized public Logger attr(Ansi.Attribute attribute) {
        console.a(attribute);
        return this;
    }

    public Logger info(Ansi.Color color, String format, Object ...args) {
        return log(System.out, color, Level.Info, format, args);
    }

    public Logger info(String format, Object ...args) {
        return log(System.out, DEFAULT, Level.Info, format, args);
    }

    public Logger warning(String format, Object ...args) {
        return log(System.err, RED, Level.Warning, format, args);
    }

    public Logger error(String format, Object ...args) {
        return log(System.err, RED, Level.Severe, format, args);
    }

    public static void println(Ansi.Color color, String format, Object ...args) {
        new Logger().log(System.out, color, Level.Info, format, args);
    }

    public static void println(Ansi.Color color, Object o) {
        new Logger().log(System.out, color, Level.Info, o.toString());
    }

    public static void println(String format, Object ...args) {
        println(YELLOW, format, args);
    }

    public static void println(Object o) {
        println(YELLOW, o.toString());
    }
}
