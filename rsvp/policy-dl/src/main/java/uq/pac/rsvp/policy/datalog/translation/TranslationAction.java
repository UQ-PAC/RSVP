package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.schema.ActionDefinition;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.datalog.ast.*;
import static uq.pac.rsvp.policy.datalog.translation.TranslationVar.*;

import java.util.ArrayList;
import java.util.List;

public class TranslationAction {
    private final TranslationRule action;
    private final TranslationRule actionPrincipal;
    private final TranslationRule actionResource;
    private final TranslationRule actionableRequests;


    public TranslationAction(TranslationSchema translationSchema) {
        Schema schema = translationSchema.getSchema();

        DLRuleDecl actionDecl = new DLRuleDecl("Action", ActionVarDecl);
        DLRuleDecl actionPrincipalDecl = new DLRuleDecl("ActionPrincipal", ActionVarDecl, PrincipalVarDecl);
        DLRuleDecl actionResourceDecl = new DLRuleDecl("ActionResource", ActionVarDecl, ResourceVarDecl);

        List<DLStatement> actionFacts = new ArrayList<>(),
                actionPrincipalRules = new ArrayList<>(),
                actionResourceRules = new ArrayList<>();
        for (String name : schema.actionNames()) {
            actionFacts.add(new DLFact(actionDecl.getName(), DLTerm.lit(name)));

            ActionDefinition def = schema.getAction(name);

            DLAtom headWP = new DLAtom(actionPrincipalDecl.getName(), DLTerm.lit(name), PrincipalVar);
            List<DLStatement> apRules = def.getAppliesToPrincipalTypes().stream()
                    .map(e -> {
                        DLRuleDecl tn = translationSchema.getTranslationEntityType(e.getName()).getEntityRuleDecl();
                        DLAtom tail = new DLAtom(tn.getName(), PrincipalVar);
                        return (DLStatement) new DLRule(headWP, tail);
                    }).toList();
            actionPrincipalRules.addAll(apRules);


            DLAtom headWR = new DLAtom(actionResourceDecl.getName(), DLTerm.lit(name), PrincipalVar);
            List<DLStatement> arRules = def.getAppliesToResourceTypes().stream()
                    .map(e -> {
                        DLRuleDecl tn = translationSchema.getTranslationEntityType(e.getName()).getEntityRuleDecl();
                        DLAtom tail = new DLAtom(tn.getName(), PrincipalVar);
                        return (DLStatement) new DLRule(headWR, tail);
                    }).toList();
            actionResourceRules.addAll(arRules);
        }

        DLRuleDecl actionableRequestsDecl = TranslationPolicy.makePolicyRuleDecl("AllActionableRequests");
        DLRule actionableRequestRule = new DLRule(
                TranslationPolicy.makePolicyAtom(actionableRequestsDecl.getName()),
                new DLAtom(actionPrincipalDecl, ActionVar, ResourceVar),
                new DLAtom(actionResourceDecl, ActionVar, ResourceVar));

        this.action = new TranslationRule(actionDecl, actionFacts);
        this.actionPrincipal = new TranslationRule(actionPrincipalDecl, actionPrincipalRules);
        this.actionResource = new TranslationRule(actionResourceDecl, actionResourceRules);
        this.actionableRequests = new TranslationRule(actionableRequestsDecl, actionableRequestRule);
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

    public TranslationRule getActionableRequests() {
        return actionableRequests;
    }
}
