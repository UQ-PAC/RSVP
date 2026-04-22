package uq.pac.rsvp;

import org.fusesource.jansi.Ansi;

import java.io.PrintStream;
import java.util.function.Function;

import static org.fusesource.jansi.Ansi.Color.*;
import static org.fusesource.jansi.Ansi.ansi;

/**
 * Very simple logger for standard streams
 */
public class StdLogger {

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
    private boolean bright;

    public StdLogger() {
        this.console = ansi();
        this.bright = false;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    synchronized private StdLogger log(PrintStream stream, Ansi.Color color, Level level, String format, Object ...args) {
        if (level.ordinal() <= this.level.ordinal()) {
            String outs = String.format(format, args);
            if (!level.desc.isEmpty()) {
                outs = level.desc + ": " + outs;
            }

            for (String formatted : outs.split("\n")) {
                Function<Ansi.Color, Ansi> fg = !this.bright ? console::fg : console::fgBright;
                fg.apply(color).a(formatted).newline();
            }
            console.reset();
            stream.print(console);
            console = ansi();
            bright = false;
        }
        return this;
    }

    synchronized public StdLogger bright() {
        this.bright = true;
        return this;
    }

    synchronized public StdLogger attr(Ansi.Attribute attribute) {
        console.a(attribute);
        return this;
    }

    synchronized public StdLogger bold() {
        console.a(Ansi.Attribute.INTENSITY_BOLD);
        return this;
    }

    synchronized public StdLogger italic() {
        console.a(Ansi.Attribute.ITALIC);
        return this;
    }

    public StdLogger finest(Ansi.Color color, String format, Object ...args) {
        return log(System.out, color, Level.Finest, format, args);
    }

    public StdLogger finer(Ansi.Color color, String format, Object ...args) {
        return log(System.out, color, Level.Finer, format, args);
    }

    public StdLogger fine(Ansi.Color color, String format, Object ...args) {
        return log(System.out, color, Level.Fine, format, args);
    }

    public StdLogger info(Ansi.Color color, String format, Object ...args) {
        return log(System.out, color, Level.Info, format, args);
    }

    public StdLogger info(String format, Object ...args) {
        return log(System.out, DEFAULT, Level.Info, format, args);
    }

    public StdLogger warning(String format, Object ...args) {
        return bold().bright().log(System.err, YELLOW, Level.Warning, format, args);
    }

    public StdLogger error(String format, Object ...args) {
        return bold().bright().log(System.err, RED, Level.Severe, format, args);
    }

    public static void println(Ansi.Color color, String format, Object ...args) {
        new StdLogger().log(System.out, color, Level.Info, format, args);
    }

    public static void println(Ansi.Color color, Object o) {
        new StdLogger().log(System.out, color, Level.Info, o.toString());
    }

    public static void println(String format, Object ...args) {
        println(YELLOW, format, args);
    }

    public static void println(Object o) {
        println(YELLOW, o.toString());
    }
}
