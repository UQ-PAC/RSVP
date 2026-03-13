package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.schema.ActionDefinition;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.datalog.ast.*;
import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Translation of actions defined in Cedar Schema to datalog.
 * The translation consists of the following
 *
 * <ul>
 *     <li> Action relation: a unary relation named 'Action' over action names in the input schema </li>
 *     <li> Action/Principal relation: a binary relation mapping actions to principal entities the action applies to </li>
 *     <li> Action/Resource relation: a binary relation mapping actions to resource entities the action applies to </li>
 *     <li> Actionable Requests: a ternary relation over principals, resources and actions that defines
 *            the space of all valid requests according to 'appliesTo' part of actions in the schema </li>
 *     <li> Facts contributing to the parentOf relation </li>
 * </ul>
 */
public class TranslationAction {
    /** Action relation */
    private final DLSegment action;
    /**
     * Action/Principal relation
     */
    private final DLSegment actionPrincipal;
    /**
     * Action/Resource relation
     */
    private final DLSegment actionResource;
    /**
     * All actionable requests relation
    */
    private final DLSegment actionableRequests;
    /**
     * Facts belonging to ParentOf rule
     */
    private final List<DLFact> actionParent;

    public TranslationAction(TranslationSchema translationSchema) {
        Schema schema = translationSchema.getSchema();

        List<DLStatement> actionFacts = new ArrayList<>(),
                actionPrincipalRules = new ArrayList<>(),
                actionResourceRules = new ArrayList<>();
        List<DLFact> actionParents = new ArrayList<>();

        for (String name : schema.actionNames()) {
            DLTerm term = DLTerm.lit(name);
            actionFacts.add(new DLFact(ActionRuleDecl, term));
            ActionDefinition def = schema.getAction(name);

            actionParents.add(new DLFact(ParentOfRuleDecl, term, term));
            def.getMemberOf().forEach(ad -> {
                actionParents.add(new DLFact(ParentOfRuleDecl, DLTerm.lit(ad.getName()), term));
            });

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

        this.action = new DLSegment(ActionRuleDecl, actionFacts);
        this.actionPrincipal = new DLSegment(ActionPrincipalRuleDecl, actionPrincipalRules);
        this.actionResource = new DLSegment(ActionResourceRuleDecl, actionResourceRules);
        this.actionableRequests = new DLSegment(ActionableRequestsRuleDecl, actionableRequestRule);
        this.actionParent = Collections.unmodifiableList(actionParents);
    }

    public Collection<DLFact> getParentOfFacts() {
        return actionParent;
    }

    public DLSegment getAction() {
        return action;
    }

    public DLSegment getActionPrincipal() {
        return actionPrincipal;
    }

    public DLSegment getActionResource() {
        return actionResource;
    }

    public DLSegment getActionableRequests() {
        return actionableRequests;
    }
}
