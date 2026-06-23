/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.support;

import org.junit.jupiter.api.Test;
import uq.pac.rsvp.Multimap;

import static org.junit.jupiter.api.Assertions.*;

public class MultimapTest {

    @Test
    void test() {
        Multimap<String, Integer> map = new Multimap<>();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());

        map.put("a", 1);
        assertFalse(map.isEmpty());
        assertEquals(1, map.size());
        assertTrue(map.containsKey("a"));
        assertFalse(map.containsKey("b"));
        assertTrue(map.containsValue("a", 1));
        assertFalse(map.containsValue("a", 2));
        assertFalse(map.containsValue("b", 1));

        map.put("a", 2);
        assertFalse(map.isEmpty());
        assertEquals(1, map.size());
        assertTrue(map.containsKey("a"));
        assertFalse(map.containsKey("b"));
        assertTrue(map.containsValue("a", 1));
        assertTrue(map.containsValue("a", 2));
        assertFalse(map.containsValue("b", 1));

        map.put("b", 1);
        assertFalse(map.isEmpty());
        assertEquals(2, map.size());
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
        assertTrue(map.containsValue("a", 1));
        assertTrue(map.containsValue("a", 2));
        assertTrue(map.containsValue("b", 1));

        map.removeValue("a", 1);
        assertFalse(map.isEmpty());
        assertEquals(2, map.size());
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
        assertFalse(map.containsValue("a", 1));
        assertTrue(map.containsValue("a", 2));
        assertTrue(map.containsValue("b", 1));

        map.removeValue("a", 1);
        assertFalse(map.isEmpty());
        assertEquals(2, map.size());
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
        assertFalse(map.containsValue("a", 1));
        assertTrue(map.containsValue("a", 2));
        assertTrue(map.containsValue("b", 1));

        map.removeValue("a", 2);
        assertFalse(map.isEmpty());
        assertEquals(2, map.size());
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
        assertFalse(map.containsValue("a", 1));
        assertFalse(map.containsValue("a", 2));
        assertTrue(map.containsValue("b", 1));

        map.removeKey("a");
        assertFalse(map.isEmpty());
        assertEquals(1, map.size());
        assertFalse(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
        assertFalse(map.containsValue("a", 1));
        assertFalse(map.containsValue("a", 2));
        assertTrue(map.containsValue("b", 1));

        map.put("a");
        assertFalse(map.isEmpty());
        assertTrue(map.get("a").isEmpty());
        assertEquals(1, map.get("b").size());
        assertEquals(2, map.size());
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
        assertFalse(map.containsValue("a", 1));
        assertFalse(map.containsValue("a", 2));
        assertTrue(map.containsValue("b", 1));
    }
}
