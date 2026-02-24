package uq.pac.rsvp.policy.datalog.translation;

import org.junit.jupiter.api.Test;
import org.logicng.formulas.Formula;
import org.logicng.formulas.FormulaFactory;
import org.logicng.formulas.Variable;
import org.logicng.transformations.dnf.DNFFactorization;

public class LogicNGTest {
    @Test
    void test() {
        FormulaFactory f = new FormulaFactory();
        Variable a = f.variable("a");
        Variable b = f.variable("b");
        Variable c = f.variable("c");
        Variable d = f.variable("d");
        Formula formula = f.and(f.or(f.and(a, b), c), d);
        Formula dnf = formula.transform(new DNFFactorization());
    }
}
