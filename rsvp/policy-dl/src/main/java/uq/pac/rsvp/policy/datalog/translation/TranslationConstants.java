package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.value.EntityUID;
import uq.pac.rsvp.policy.ast.expr.VariableExpression;
import uq.pac.rsvp.policy.ast.schema.EntityTypeDefinition;
import uq.pac.rsvp.policy.datalog.ast.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Constants used throughout the translation of Cedar to datalog
 */
public class TranslationConstants {
    /**
     * Name for the datalog specification the translation to generate
     */
    public static String ProgramName = "auth.dl";

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
    public static DLAtom makeStandardAtom(DLRuleDecl decl) {
        return makeStandardAtom(decl, false);
    }

    public static DLAtom makeStandardAtom(DLRuleDecl decl, boolean negate) {
        return new DLAtom(decl, negate, PrincipalVar, ResourceVar, ActionVar);
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
            makeStandardRuleDecl("ActionableRequests");

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
    public static DLSegment makePermittedRequestsRule() {
        DLRule rule = new DLRule(makeStandardAtom(PermittedRequestsRuleDecl),
                makeStandardAtom(PermitRuleDecl),
                makeStandardAtom(ForbidRuleDecl, true));
        return new DLSegment(PermittedRequestsRuleDecl, rule);
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
    public static DLSegment makeForbiddenRequestsRule() {
        DLRule rule = new DLRule(makeStandardAtom(ForbiddenRequestsRuleDecl),
                makeStandardAtom(ActionableRequestsRuleDecl),
                makeStandardAtom(PermittedRequestsRuleDecl, true));
        return new DLSegment(ForbiddenRequestsRuleDecl, rule);
    }

    /**
     * Parent/child hierarchy of entities
     */
    public final static DLRuleDecl ParentOfRuleDecl =
            new DLRuleDecl("ParentOf",
                    DLDeclTerm.symbolic("parent"),
                    DLDeclTerm.symbolic("child"));

    public final static Character OUTPUT_DELIMITER = '\t';

    public static List<DLOutputDirective> makeIODirectives(Collection<DLRuleDecl> decls) {
        List<DLRuleDecl> output = new ArrayList<>(decls);
        output.addAll(List.of(
                ActionPrincipalRuleDecl,
                ActionResourceRuleDecl,
                PermitRuleDecl,
                ForbidRuleDecl,
                PermittedRequestsRuleDecl,
                ForbiddenRequestsRuleDecl,
                ActionableRequestsRuleDecl));
        return output.stream().map(DLOutputDirective::new).toList();
    }

	/**
	 * Get a declaration for a unary entity relation
	*/
    public static DLRuleDecl getEntityRuleDecl(EntityTypeDefinition entity) {
        return new DLRuleDecl("Entity_" + entity.getName().replace(':', '_'), DLType.SYMBOL);
    }

	/**
     * Declaration for a ternary attribute relation that associates
	 * entities to attribute names and respective values. Notably, this relation only records
     * attributes if there are associated values. For instance, for the case when an attribute is
     * mapped to an empty list it will not be tracked.
	*/
    public static DLRuleDecl AttributeRuleDecl =
            new DLRuleDecl("Attribute",
                    DLDeclTerm.symbolic("uid"),
                    DLDeclTerm.symbolic("attr"),
                    DLDeclTerm.symbolic("value"));

    /**
     * As per above Attribute relation we need to be able to tell whether an entity
     * has an attribute regardless if there are associated values or not. The below
     * declaration does exactly that. It maps EUIDs to the attributes it has
     */
    public static DLRuleDecl HasAttributeRuleDecl =
            new DLRuleDecl("HasAttribute",
                    DLDeclTerm.symbolic("uid"),
                    DLDeclTerm.symbolic("attr"));

    /**
     * Temporary entity type. This type is used to generate internal EUID's
     * to aid with record/atribute processing
     */
    public static EntityTypeDefinition TmpRecordType =
            new EntityTypeDefinition("RSVP::DL::Tmp::Record::Attr::Type");

    /**
     * Generate a random EUID based on the {@link TranslationConstants#TmpRecordType}
     */
    public static EntityUID getRandomTmpEUID() {
        return getEUID(TmpRecordType, UUID.randomUUID().toString());
    }

    /**
     * Get a UID from a definition
     */
    public static EntityUID getEUID(EntityTypeDefinition def, String uid) {
        return EntityUID.parse("%s::\"%s\"".formatted(def.getName(), uid)).orElseThrow();
    }

    /**
     * UID name denoting an "unknown entity"
     */
    public static String UndefinedEntityUIDName = "???";

    /**
     * Get an unknown entity (UID) only. Abstraction over entities that can be given for
     * authorisation but are not in the provided list of entities
     */
    public static EntityUID getUndefinedEUID(TranslationEntityDefinition def) {
        return getEUID(def.getEntityDefinition(), UndefinedEntityUIDName);
    }

}
