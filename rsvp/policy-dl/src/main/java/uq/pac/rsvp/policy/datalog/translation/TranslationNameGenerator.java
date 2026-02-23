package uq.pac.rsvp.policy.datalog.translation;

public class TranslationNameGenerator {
    private static int INDEX = 0;
    private static final String VAR_BASENAME = "__rsvp_var";
    public static synchronized String getVar() {
        return VAR_BASENAME + INDEX++;
    }
}
