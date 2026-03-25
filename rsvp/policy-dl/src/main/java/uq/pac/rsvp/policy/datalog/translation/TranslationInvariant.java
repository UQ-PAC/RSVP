package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.expr.Expression;
import uq.pac.rsvp.policy.datalog.ast.DLRule;
import uq.pac.rsvp.policy.datalog.ast.DLRuleDecl;
import uq.pac.rsvp.policy.datalog.invariant.Invariant;

import java.util.List;

/**
 * Translation of an invariant to Datalog. The translation is similar to policies, except
 * rather than operating on a list of pre-defined variables, in invariants variables,
 * as well as the arity of the generated rules are dynamic
 */
public class TranslationInvariant {
    /**
     * Generated Declaration
     */
    private final DLRuleDecl declaration;
    /**
     * Generated policy rules
     */
    private final List<DLRule> rules;

    public TranslationInvariant(Invariant invariant, TranslationSchema schema) {
        this.declaration = TranslationConstants.makeInvariantRuleDecl(invariant);
        Expression transformed = TranslationTransformer.transform(invariant.getExpression());
        List<List<Expression>> disjunctions = NFConverter.toDNF(transformed);
        this.rules = disjunctions.stream()
                .map(disjunction -> TranslationVisitor.translateInvariant(schema, disjunction, declaration, invariant.getQuantifier()))
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
