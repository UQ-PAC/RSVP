package uq.pac.rsvp;

import java.util.*;

import static uq.pac.rsvp.Assertion.require;

/**
 * A very-simple implementation of hash-based multimap.
 * The key difference with a multimap in Guava is that
 * the mapping can exist even if it is not associated with any keys
 */
public class Multimap<K, V> {
    private final Map<K, Set<V>> data;

    public Multimap() {
        this.data = new HashMap<>();
    }

    /**
     * @return Number of mappings (even if empty)
     */
    public int size() {
        return data.size();
    }


    public boolean isEmpty() {
        return data.isEmpty();
    }

    /**
     * @return {@code true} if the multimap contains the specified key,
     * even if all its associated values have been removed
     */
    public boolean containsKey(K key) {
        return data.containsKey(key);
    }

    /**
     *
     * @return {@code true} if the map contains a specified value/pair
     */
    public boolean containsValue(K key, V value) {
        Set<V> values = data.get(key);
        if (values != null) {
            return values.contains(value);
        }
        return false;
    }

    /**
     * Insert a specified key into the multimap.
     * The key is mapped to an empty set of values
     */
    public void put(K key) {
        if (!data.containsKey(key)) {
            data.put(key, new HashSet<>());
        }
    }

    /**
     * Insert a specified key/value pair
     */
    public void put(K key, V value) {
        put(key);
        require(data.containsKey(key));
        data.get(key).add(value);
    }

    /**
     * Associate a given key with a collection of values
     */
    public void put(K key, Collection<V> value) {
        if (data.containsKey(key)) {
            data.get(key).addAll(value);
        } else {
            data.put(key, new HashSet<>(value));
        }
    }

    @SafeVarargs
    public final void put(K key, V... value) {
        put(key, Arrays.asList(value));
    }

    /**
     * Remove a given key (with all its mappings) from the multimap
     */
    public void removeKey(K key) {
        data.remove(key);
    }

    /**
     * Remove a given key
     * @throws AssertionError if a key for the specified values does not exist
     */
    public void removeValue(K key, V value) {
        if (data.containsKey(key)) {
            data.get(key).remove(value);
        } else {
            throw new AssertionError("Attempting removal of a non-existing key: " + key.toString());
        }
    }

    /**
     * Return a set of mappings for a given value
     */
    public Set<V> get(K key) {
        return data.get(key);
    }

    /**
     * Return the multimap as a map
     */
    public Map<K, Set<V>> asMap() {
        return Map.copyOf(data);
    }

}
