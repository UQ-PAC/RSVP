package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.expr.Expression;
import uq.pac.rsvp.policy.datalog.ast.DLDeclTerm;
import uq.pac.rsvp.policy.datalog.ast.DLRule;
import uq.pac.rsvp.policy.datalog.ast.DLRuleDecl;
import uq.pac.rsvp.policy.datalog.ast.DLType;
import static uq.pac.rsvp.policy.ast.expr.VariableExpression.Reference.*;

import java.util.List;

public class TranslationPolicy {
   private final DLRuleDecl declaration;
   private final List<DLRule> rules;

   public static DLRuleDecl makeRulePolicyDecl(String name) {
       return new DLRuleDecl(name,
               new DLDeclTerm(Principal.getValue(), DLType.SYMBOL),
               new DLDeclTerm(Resource.getValue(), DLType.SYMBOL),
               new DLDeclTerm(Action.getValue(), DLType.SYMBOL));
   }

   public TranslationPolicy(String name, Policy policy, TranslationSchema schema) {
       this.declaration = makeRulePolicyDecl(name);
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
}
