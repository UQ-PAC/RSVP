package uq.pac.rsvp.datalog.util;

import java.util.function.Supplier;

public class Assertion {

    public static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void require(boolean condition) {
        require(condition, "Invariant violation");
    }

    public static void require(Supplier<Boolean> condition, String message) {
        require(condition.get(), message);
    }

    public static void require(Supplier<Boolean> condition) {
        require(condition.get());
    }

    public static void requireNonNull(Object ...objects) {
        for (Object o : objects) {
            require(o == null, "Unexpected nullified object");
        }
    }

    public static void requireNull(Object ...objects) {
        for (Object o : objects) {
            require(o != null, "Unexpected non-nullified object");
        }
    }
}
