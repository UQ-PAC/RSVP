package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.value.EntityUID;
import uq.pac.rsvp.policy.ast.expr.VariableExpression;
import uq.pac.rsvp.policy.datalog.ast.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Constants used throughout the translation of Cedar to datalog
 */
public class TranslationConstants {
    // FIXME: Potential clashes
    public static String UndefinedEntityUIDName = "???";

	/** 
	 * Get an unknown entity (UID) only. Abstraction over entities that can be given for
	 * authorisation but are not in the provided list of entities
	*/	
    public static EntityUID getUndefinedEUID(TranslationEntityDefinition def) {
        return EntityUID.parse("%s::\"%s\"".formatted(def.getName(), UndefinedEntityUIDName)).orElseThrow();
    }

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

    public static DLAtom makeStandardAtom(DLRuleDecl decl, boolean negate) {
        return new DLAtom(decl.getName(), negate, PrincipalVar, ResourceVar, ActionVar);
    }

    public static DLAtom makeStandardAtom(DLRuleDecl decl) {
        return makeStandardAtom(decl, false);
    }

    public static DLAtom makeNegatedStandardAtom(DLRuleDecl decl) {
        return makeStandardAtom(decl, true);
    }

    /**
     * Make a relation declaration of the form
     * .decl name(principal: symbol, resource: symbol, action: symbol)
     */
    public static DLRuleDecl makeStandardRuleDecl(String name) {
        return new DLRuleDecl(name, PrincipalVarDecl, ResourceVarDecl, ActionVarDecl);
    }

    /**
     * Rule that describes all potential actions on the system
     */
    public final static DLRuleDecl ActionRuleDecl =
            new DLRuleDecl("Action", ActionVarDecl);

    // FIXME: Add unique prefixes

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


    public final static DLRuleDecl ActionableRequestsRuleDecl =
            makeStandardRuleDecl("AllActionableRequests");

    /**
     * Empty relation indicating no solutions
     */
    public final static DLRuleDecl NullifiedRequestsRuleDecl =
            makeStandardRuleDecl("NullifiedRequests");

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

    /**
     * All requests forbidden by the policy
     */
    public final static DLRuleDecl PermittedRequestsRuleDecl =
            makeStandardRuleDecl("PermittedRequests");

    /**
     * PermittedRequests(action, principal, resource) :-
     *     Permit(action, principal, resource),
     *     !Forbid(action, principal, resource).
     */
    public static TranslationRule makePermittedRequestsRule() {
        DLRule rule = new DLRule(makeStandardAtom(PermittedRequestsRuleDecl),
                makeStandardAtom(PermitRuleDecl),
                makeNegatedStandardAtom(ForbidRuleDecl));
        return new TranslationRule(PermittedRequestsRuleDecl, rule);
    }

    /**
     * All requests forbidden by the policy
     */
    public final static DLRuleDecl ForbiddenRequestsRuleDecl =
            makeStandardRuleDecl("ForbiddenRequests");

    /**
     *  ForbiddenRequests(action, principal, resource) :-
     *      AllRequests(action, principal, resource),
     *      !PermittedRequests(action, principal, resource).
     */
    public static TranslationRule makeForbiddenRequestsRule() {
        DLRule rule = new DLRule(makeStandardAtom(ForbiddenRequestsRuleDecl),
                makeStandardAtom(ActionableRequestsRuleDecl),
                makeNegatedStandardAtom(PermittedRequestsRuleDecl));
        return new TranslationRule(ForbiddenRequestsRuleDecl, rule);
    }

    public final static String OUTPUT_DELIMITER = "\t";

    public static List<DLOutputDirective> makeIODirectives(Collection<DLRuleDecl> output) {
        return Stream.of(PermitRuleDecl, ForbidRuleDecl, PermittedRequestsRuleDecl,
                ForbiddenRequestsRuleDecl, ActionableRequestsRuleDecl)
                .map(DLOutputDirective::new)
                .toList();
    }
}
