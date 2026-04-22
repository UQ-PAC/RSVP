package uq.pac.rsvp.policy.datalog.translation;

import java.util.List;

import static uq.pac.rsvp.Assertion.require;

/**
 * Representation of a datalog relation as rows of string lists
 * For performance reasons it is mutable and does not check whether
 * tuples are unique. The only requirement is that the headers and the
 * rows have the same arity.
 */
public class Relation {
    private final List<String> headers;
    private final List<List<String>> rows;

    public Relation(List<String> headers, List<List<String>> rows) {
        this.headers = headers;
        this.rows = rows;

        int size = headers.size();
        rows.forEach(r -> require(r.size() == size));
    }

    public List<List<String>> getRows() {
        return rows;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public int size() {
        return rows.size();
    }

    public boolean isEmpty() {
        return rows.isEmpty();
    }

    public int arity() {
        return headers.size();
    }
}
