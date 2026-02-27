package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.schema.ActionDefinition;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.datalog.ast.*;
import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.*;

import java.util.ArrayList;
import java.util.List;

public class TranslationAction {
    private final TranslationRule action;
    private final TranslationRule actionPrincipal;
    private final TranslationRule actionResource;
    private final TranslationRule actionableRequests;

    public TranslationAction(TranslationSchema translationSchema) {
        Schema schema = translationSchema.getSchema();

        List<DLStatement> actionFacts = new ArrayList<>(),
                actionPrincipalRules = new ArrayList<>(),
                actionResourceRules = new ArrayList<>();
        for (String name : schema.actionNames()) {
            actionFacts.add(new DLFact(ActionRuleDecl, DLTerm.lit(name)));
            ActionDefinition def = schema.getAction(name);

            DLAtom headWP = new DLAtom(ActionPrincipalRuleDecl, DLTerm.lit(name), PrincipalVar);
            List<DLStatement> apRules = def.getAppliesToPrincipalTypes().stream()
                    .map(e -> {
                        DLRuleDecl tn = translationSchema.getTranslationEntityType(e.getName()).getEntityRuleDecl();
                        DLAtom tail = new DLAtom(tn.getName(), PrincipalVar);
                        return (DLStatement) new DLRule(headWP, tail);
                    }).toList();
            actionPrincipalRules.addAll(apRules);


            DLAtom headWR = new DLAtom(ActionResourceRuleDecl, DLTerm.lit(name), PrincipalVar);
            List<DLStatement> arRules = def.getAppliesToResourceTypes().stream()
                    .map(e -> {
                        DLRuleDecl tn = translationSchema.getTranslationEntityType(e.getName()).getEntityRuleDecl();
                        DLAtom tail = new DLAtom(tn.getName(), PrincipalVar);
                        return (DLStatement) new DLRule(headWR, tail);
                    }).toList();
            actionResourceRules.addAll(arRules);
        }

        DLRule actionableRequestRule = new DLRule(makeStandardAtom(AllActionableRequestsRuleDecl),
                new DLAtom(ActionPrincipalRuleDecl, ActionVar, ResourceVar),
                new DLAtom(ActionResourceRuleDecl, ActionVar, ResourceVar));

        this.action = new TranslationRule(ActionRuleDecl, actionFacts);
        this.actionPrincipal = new TranslationRule(ActionPrincipalRuleDecl, actionPrincipalRules);
        this.actionResource = new TranslationRule(ActionResourceRuleDecl, actionResourceRules);
        this.actionableRequests = new TranslationRule(AllActionableRequestsRuleDecl, actionableRequestRule);
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
