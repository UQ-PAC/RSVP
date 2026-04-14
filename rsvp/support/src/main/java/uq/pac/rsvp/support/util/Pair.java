package uq.pac.rsvp.support.util;

import java.util.AbstractMap;

public class Pair<A, B> extends AbstractMap.SimpleEntry<A, B> {
    public Pair(A a, B b) {
        super(a, b);
    }
}
