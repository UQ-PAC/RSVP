package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.value.EntityUID;
import uq.pac.rsvp.policy.datalog.ast.DLVar;

/**
 * FIXME: Need to ensure the names are unique WRT provided program
 * Unique name generator
 */
public class Naming {
    private static final int[] INDEX = new int [2];

    public static synchronized DLVar getDLVar() {
        return new DLVar("var" + INDEX[0]++);
    }

    public static synchronized EntityUID getEUID() {
        return EntityUID.parse("Tmp::Record::\"%d\"".formatted(INDEX[1]++)).orElseThrow();
    }
}
