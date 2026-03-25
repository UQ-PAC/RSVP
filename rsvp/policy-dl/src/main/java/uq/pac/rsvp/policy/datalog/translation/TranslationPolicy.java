package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.expr.Expression;
import uq.pac.rsvp.policy.datalog.ast.*;

import java.util.List;

/**
 * Translation of a Cedar policy to datalog
 * <p>
 * Each Cedar policy is translated into a ternary declaration over principals
 * resources and actions. A policy (represented as a single Cedar expression)
 * is translated into datalog is follows:
 * <ul>
 *     <li> Generate a ternary-term rule declaration for the policy </li>
 *     <li> Convert the policy expression to DNF and generate a separate rule for each conjunction </li>
 * </ul>
 */
public class TranslationPolicy {
    /**
     * Generated Declaration
     */
    private final DLRuleDecl declaration;
    /**
     * Generated policy rules
     */
    private final List<DLRule> rules;

    public TranslationPolicy(Policy policy, TranslationSchema schema) {
        this.declaration = TranslationConstants.makePolicyRuleDecl(policy);
        Expression transformed = TranslationTransformer.transform(policy.getCondition());
        List<List<Expression>> disjunctions = NFConverter.toDNF(transformed);
        this.rules = disjunctions.stream()
                .map(disjunction -> TranslationVisitor.translatePolicy(schema, disjunction, declaration))
                .toList();
    }

    public List<DLRule> getRules() {
       return rules;
   }

    public DLRuleDecl getDeclaration() {
       return declaration;
   }

    public String getName() {
        return declaration.getName();
   }
}
