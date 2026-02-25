package uq.pac.rsvp.policy.datalog.translation;

public class TranslationError extends RuntimeException {

    public TranslationError(String message) {
        super(message);
    }

    public static void error(boolean condition, String message) {
        if (!condition) {
            throw new TranslationError("Translation error: " + message);
        }
    }

    public static void error(String message) {
        throw new TranslationError("Translation error: " + message);
    }
}
