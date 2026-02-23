package uq.pac.rsvp.policy.datalog.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class Util {

    public static <T> Predicate<T> distinctBy(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    static <E> E required(Object o, Class<E> cls) {
        if (cls.isInstance(o)) {
            return cls.cast(o);
        }
        throw new RuntimeException("Unsupported type: " + o.getClass().getSimpleName());
    }

    public static boolean instanceOf(Object o, Class<?> ... cls) {
        for (Class<?> c : cls) {
            if (c.isInstance(o)) {
                return true;
            }
        }
        return false;
    }
}
