package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.expr.Expression;
import uq.pac.rsvp.policy.datalog.ast.*;

import java.util.List;

public class TranslationPolicy {
    private final DLRuleDecl declaration;
    private final List<DLRule> rules;

    public TranslationPolicy(String name, Policy policy, TranslationSchema schema) {
        this.declaration = TranslationConstants.makeStandardRuleDecl(name);
        List<List<Expression>> disjunctions = NFConverter.toDNF(policy.getCondition());
        this.rules = disjunctions.stream().map(disjunction -> {
            return TranslationVisitor.translate(schema, disjunction, declaration);
        }).toList();
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
