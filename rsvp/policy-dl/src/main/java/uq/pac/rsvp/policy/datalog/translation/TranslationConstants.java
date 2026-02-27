package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.expr.VariableExpression;
import uq.pac.rsvp.policy.datalog.ast.*;

/**
 * Constants used throughout the translation of Cedar to datalog
 */
public class TranslationConstants {
    /**
     * The translation revolves around rules of the form using principal, resource
     * and action cedar variables.
     */
    public final static DLVar PrincipalVar =
            new DLVar(VariableExpression.Reference.Principal.getValue());
    public final static DLVar ResourceVar =
            new DLVar(VariableExpression.Reference.Resource.getValue());
    public final static DLVar ActionVar =
            new DLVar(VariableExpression.Reference.Action.getValue());

    /**
     * Variable declarations. For simplicity, we use symbolic Datalog types.
     * Perhaps it could be better to type variables based on types of entities
     * and actions.
     */
    public final static DLDeclTerm PrincipalVarDecl =
            DLDeclTerm.symbolic(PrincipalVar.getName());
    public final static DLDeclTerm ResourceVarDecl =
            DLDeclTerm.symbolic(ResourceVar.getName());
    public final static DLDeclTerm ActionVarDecl =
            DLDeclTerm.symbolic(ActionVar.getName());

    /**
     * Make an atom of the form <name>(principal, resource, action)
    */
    public static DLAtom makeStandardAtom(String name) {
        return new DLAtom(name, PrincipalVar, ResourceVar, ActionVar);
    }

    public static DLAtom makeStandardAtom(DLRuleDecl decl) {
        return new DLAtom(decl.getName(), PrincipalVar, ResourceVar, ActionVar);
    }

    /**
     * Make a relation declaration of the form
     * .decl name(principal: symbol, resource: symbol, action: symbol)
     */
    public static DLRuleDecl makeStandardRuleDecl(String name) {
        return new DLRuleDecl(name, PrincipalVarDecl, ResourceVarDecl, ActionVarDecl);
    }

    /**
     * Rule that describes all potential principals on the system
     */
    public final static DLRuleDecl PrincipalRuleDecl =
            new DLRuleDecl("Principal", PrincipalVarDecl);

    /**
     * Rule that describes all potential resources on the system
     */
    public final static DLRuleDecl ResourceRuleDecl =
            new DLRuleDecl("Resource", ResourceVarDecl);

    /**
     * Rule that describes all potential actions on the system
     */
    public final static DLRuleDecl ActionRuleDecl =
            new DLRuleDecl("Action", ActionVarDecl);

    /**
     * Rule that captures relation between actions and principals,
     * ActionPrincipal(A, P) means that some action 'A' considers 'P' be a principal
     */
    public final static DLRuleDecl ActionPrincipalRuleDecl =
            new DLRuleDecl("ActionPrincipal", ActionVarDecl, PrincipalVarDecl);

    /**
     * Rule that captures relation between actions and resources,
     * ActionPrincipal(A, R) means that some resource 'R' considers 'R' be a resource.
     */
    public final static DLRuleDecl ActionResourceRuleDecl =
            new DLRuleDecl("ActionResource", ActionVarDecl, ResourceVarDecl);


    public final static DLRuleDecl AllActionableRequestsRuleDecl =
            makeStandardRuleDecl("AllActionableRequests");

    /**
     * Rule that describes all potential requests (even impossible ones fom the point of Actions)
     */
    public final static DLRuleDecl AllRequestsRuleDecl =
            makeStandardRuleDecl("AllRequests");

    /**
     * Rule that describes all explicitly permitted requests
     */
    public final static DLRuleDecl PermitRuleDecl =
            makeStandardRuleDecl("Permit");

    /**
     * Rule that describes all explicitly forbidden requests
     */
    public final static DLRuleDecl ForbidRuleDecl =
            makeStandardRuleDecl("Forbid");
}
