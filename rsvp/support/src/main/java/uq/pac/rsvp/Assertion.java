package uq.pac.rsvp;

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

    public static <E> E require(Object o, Class<E> cls) {
        if (cls.isInstance(o)) {
            return cls.cast(o);
        }
        throw new AssertionError("Unexpected type: " + o.getClass().getSimpleName());
    }
}
