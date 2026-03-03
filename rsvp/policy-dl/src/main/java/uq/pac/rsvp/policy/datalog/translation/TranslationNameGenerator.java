package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.datalog.ast.DLVar;

public class TranslationNameGenerator {
    private static int VAR_INDEX = 0;
    private static final String VAR_BASENAME = "__rsvp_dl_var";

    public static synchronized DLVar getVar() {
        return new DLVar(VAR_BASENAME + VAR_INDEX++);
    }
}
