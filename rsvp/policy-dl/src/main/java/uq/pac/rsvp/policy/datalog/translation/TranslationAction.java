package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.schema.ActionDefinition;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.datalog.ast.*;
import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Translation of actions defined in Cedar Schema to datalog.
 * The translation consists of 4 relations
 *
 * <ul>
 *     <li> Action relation: a unary relation named 'Action' over action names in the input schema </li>
 *     <li> Action/Principal relation: a binary relation mapping actions to principal entities the action applies to </li>
 *     <li> Action/Resource relation: a binary relation mapping actions to resource entities the action applies to </li>
 *     <li> Actionable Requests: a ternary relation over principals, resources and actions that defines
 *            the space of all valid requests according to 'appliesTo' part of actions in the schema </li>
 * </ul>
 */
public class TranslationAction {
    /** Action relation */
    private final TranslationRule action;
    /**
     * Action/Principal relation
     */
    private final TranslationRule actionPrincipal;
    /**
     * Action/Resource relation
     */
    private final TranslationRule actionResource;
    /**
     * All actionable requests relation
    */
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

        DLRule actionableRequestRule = new DLRule(makeStandardAtom(ActionableRequestsRuleDecl),
                new DLAtom(ActionPrincipalRuleDecl, ActionVar, PrincipalVar),
                new DLAtom(ActionResourceRuleDecl, ActionVar, ResourceVar));

        this.action = new TranslationRule(ActionRuleDecl, actionFacts);
        this.actionPrincipal = new TranslationRule(ActionPrincipalRuleDecl, actionPrincipalRules);
        this.actionResource = new TranslationRule(ActionResourceRuleDecl, actionResourceRules);
        this.actionableRequests = new TranslationRule(ActionableRequestsRuleDecl, actionableRequestRule);
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
