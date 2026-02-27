package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.expr.Expression;
import uq.pac.rsvp.policy.ast.expr.VariableExpression;
import uq.pac.rsvp.policy.datalog.ast.*;

import static uq.pac.rsvp.policy.ast.expr.VariableExpression.Reference.*;

import java.util.List;

public class TranslationPolicy {
    private final DLRuleDecl declaration;
    private final List<DLRule> rules;

    public static List<VariableExpression.Reference> VARIABLES =
            List.of(Principal, Resource, Action);

    public static List<DLTerm> TERMS = VARIABLES.stream()
            .map(v -> DLTerm.var(v.getValue())).toList();

    public static List<DLDeclTerm> DECL_TERMS = VARIABLES.stream()
            .map(v -> new DLDeclTerm(v.getValue(), DLType.SYMBOL))
            .toList();

    public static DLAtom makePolicyAtom(String name) {
        return new DLAtom(name, TERMS);
    }

    public static DLRuleDecl makePolicyRuleDecl(String name) {
        return new DLRuleDecl(name, DECL_TERMS);
    }

    public TranslationPolicy(String name, Policy policy, TranslationSchema schema) {
        this.declaration = makePolicyRuleDecl(name);
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
