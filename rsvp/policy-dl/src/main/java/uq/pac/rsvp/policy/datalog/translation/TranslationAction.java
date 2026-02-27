package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.schema.ActionDefinition;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.datalog.ast.*;

import java.util.ArrayList;
import java.util.List;


import static uq.pac.rsvp.policy.ast.expr.VariableExpression.Reference.*;

public class TranslationAction {
    private final TranslationRule action;
    private final TranslationRule actionPrincipal;
    private final TranslationRule actionResource;

    private static final DLTerm PRINCIPAL = DLTerm.var(Principal.getValue());
    private static final DLTerm RESOURCE = DLTerm.var(Resource.getValue());

    public TranslationAction(TranslationSchema translationSchema) {
        DLDeclTerm actionTerm = DLDeclTerm.symbolic(Action.getValue()),
                principalTerm = DLDeclTerm.symbolic(Principal.getValue()),
                resourceTerm = DLDeclTerm.symbolic(Resource.getValue());

        Schema schema = translationSchema.getSchema();

        DLRuleDecl actionDecl = new DLRuleDecl("Action", actionTerm);
        DLRuleDecl actionPrincipalDecl = new DLRuleDecl("ActionPrincipal", actionTerm, principalTerm);
        DLRuleDecl actionResourceDecl = new DLRuleDecl("ActionResource", actionTerm, resourceTerm);

        List<DLStatement> actionFacts = new ArrayList<>(),
                actionPrincipalRules = new ArrayList<>(),
                actionResourceRules = new ArrayList<>();
        for (String name : schema.actionNames()) {
            actionFacts.add(new DLFact(actionDecl.getName(), DLTerm.lit(name)));

            ActionDefinition def = schema.getAction(name);

            DLAtom headWP = new DLAtom(actionPrincipalDecl.getName(), DLTerm.lit(name), PRINCIPAL);
            List<DLStatement> apRules = def.getAppliesToPrincipalTypes().stream()
                    .map(e -> {
                        DLRuleDecl tn = translationSchema.getTranslationEntityType(e.getName()).getEntityRuleDecl();
                        DLAtom tail = new DLAtom(tn.getName(), PRINCIPAL);
                        return (DLStatement) new DLRule(headWP, tail);
                    }).toList();
            actionPrincipalRules.addAll(apRules);


            DLAtom headWR = new DLAtom(actionResourceDecl.getName(), DLTerm.lit(name), RESOURCE);
            List<DLStatement> arRules = def.getAppliesToResourceTypes().stream()
                    .map(e -> {
                        DLRuleDecl tn = translationSchema.getTranslationEntityType(e.getName()).getEntityRuleDecl();
                        DLAtom tail = new DLAtom(tn.getName(), RESOURCE);
                        return (DLStatement) new DLRule(headWR, tail);
                    }).toList();
            actionResourceRules.addAll(arRules);
        }

        this.action = new TranslationRule(actionDecl, actionFacts);
        this.actionPrincipal = new TranslationRule(actionPrincipalDecl, actionPrincipalRules);
        this.actionResource = new TranslationRule(actionResourceDecl, actionResourceRules);
    }

    public TranslationRule getAction() {
        return action;
    }

    public TranslationRule getActionPrincipal() {
        return actionPrincipal;
    }

    public TranslationRule getActionResource() {
        return actionResource;
    }
}
