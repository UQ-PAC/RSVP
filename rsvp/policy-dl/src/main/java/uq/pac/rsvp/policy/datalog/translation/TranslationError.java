package uq.pac.rsvp.policy.datalog.translation;

public class TranslationError extends RuntimeException {

    public TranslationError(String message) {
        super(message);
    }

    public TranslationError() {
        super();
    }

    public TranslationError(String format, Object ...args) {
        super(String.format(format, args));
    }

    public static void error(boolean condition, String format, Object ...args) {
        if (!condition) {
            throw new TranslationError("Translation error: " + format, args);
        }
    }

    public static void error(String message) {
        throw new TranslationError("Translation error: " + message);
    }

    public static void error(boolean cond, String message) {
        if (!cond) {
            throw new TranslationError("Translation error: " + message);
        }
    }
}
