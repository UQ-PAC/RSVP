package uq.pac.rsvp.policy.ast;

import static uq.pac.rsvp.Assertion.require;

public class Util {
    public static String unquote(String s) {
        if (s == null) {
            return null;
        }
        require(s.length() >= 2);
        require(s.charAt(0) == '"');
        require(s.charAt(s.length() - 1) == '"');
        return s.substring(1, s.length() - 1);
    }
}
