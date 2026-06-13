package uq.pac.rsvp.policy.datalog.logic;

import org.junit.jupiter.api.Test;

public class FormulaTest {


    @Test
    void test() {
        Predicate<String> a = new Predicate<>("a");
        Predicate<String> b = new Predicate<>("b");
        Predicate<String> c = new Predicate<>("c");
        Predicate<String> d = new Predicate<>("d");

        Formula c1 = new Negation(new Disjunction(a, b));
        Formula c2 = new Negation(new Disjunction(c, d));

        Formula f = new Conjunction(c1, c2);

        System.out.println(f);


    }
}
