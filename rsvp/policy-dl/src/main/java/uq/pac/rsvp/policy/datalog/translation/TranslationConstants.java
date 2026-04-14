package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.value.EntityUID;
import uq.pac.rsvp.policy.ast.entity.EntityReference;
import uq.pac.rsvp.policy.ast.schema.EntityTypeDefinition;
import uq.pac.rsvp.policy.datalog.ast.*;
import uq.pac.rsvp.policy.datalog.invariant.Invariant;

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
    public final static DLVar PrincipalVar = new DLVar("principal");
    public final static DLVar ResourceVar = new DLVar("resource");
    public final static DLVar ActionVar = new DLVar("action");

    /**
     * Variable declarations. For simplicity, we use symbolic Datalog types.
     * Perhaps it could be better to type variables based on types of entities
     * and actions.
     */
    public final static DLDeclTerm PrincipalVarDecl = DLDeclTerm.symbolic(PrincipalVar.getName());
    public final static DLDeclTerm ResourceVarDecl = DLDeclTerm.symbolic(ResourceVar.getName());
    public final static DLDeclTerm ActionVarDecl = DLDeclTerm.symbolic(ActionVar.getName());

    /**
     * Make a (potentially negated) atom over the terms of the declaration
     */
    public static DLAtom makeAtom(DLRuleDecl decl, boolean negated) {
        List<DLTerm> terms =
                decl.getDeclTerms().stream().map(t -> DLTerm.var(t.getName())).toList();
        return new DLAtom(decl, negated, terms);
    }

    /**
     * Make an atom over the terms of the declaration
     */
    public static DLAtom makeAtom(DLRuleDecl decl) {
        return makeAtom(decl, false);
    }

    /**
     * Make a relation declaration of the form
     * .decl name(principal: symbol, resource: symbol, action: symbol)
     */
    public static DLRuleDecl makePolicyRuleDecl(String name) {
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
            makePolicyRuleDecl("ActionableRequests");

    /**
     * Empty relation indicating no solutions
     */
    public final static DLRuleDecl NullifiedRequestsRuleDecl =
            makePolicyRuleDecl("NullifiedRequests");

    /**
     * Rule that describes all explicitly permitted requests
     */
    public final static DLRuleDecl PermitRuleDecl =
            makePolicyRuleDecl("Permit");

    /**
     * Rule that describes all explicitly forbidden requests
     */
    public final static DLRuleDecl ForbidRuleDecl =
            makePolicyRuleDecl("Forbid");

    /**
     * All requests forbidden by the policy
     */
    public final static DLRuleDecl PermittedRequestsRuleDecl =
            makePolicyRuleDecl("PermittedRequests");

    /**
     * PermittedRequests(action, principal, resource) :-
     *     Permit(action, principal, resource),
     *     !Forbid(action, principal, resource).
     */
    public static DLSegment makePermittedRequestsRule() {
        DLRule rule = new DLRule(makeAtom(PermittedRequestsRuleDecl),
                makeAtom(PermitRuleDecl),
                makeAtom(ForbidRuleDecl, true));
        return new DLSegment(PermittedRequestsRuleDecl, rule);
    }

    /**
     * All requests forbidden by the policy
     */
    public final static DLRuleDecl ForbiddenRequestsRuleDecl =
            makePolicyRuleDecl("ForbiddenRequests");

    /**
     *  ForbiddenRequests(action, principal, resource) :-
     *      AllRequests(action, principal, resource),
     *      !PermittedRequests(action, principal, resource).
     */
    public static DLSegment makeForbiddenRequestsRule() {
        DLRule rule = new DLRule(makeAtom(ForbiddenRequestsRuleDecl),
                makeAtom(ActionableRequestsRuleDecl),
                makeAtom(PermittedRequestsRuleDecl, true));
        return new DLSegment(ForbiddenRequestsRuleDecl, rule);
    }

    /**
     * Parent/child hierarchy of entities
     */
    public final static DLRuleDecl ParentOfRuleDecl =
            new DLRuleDecl("ParentOf",
                    DLDeclTerm.symbolic("parent"),
                    DLDeclTerm.symbolic("child"));

    public final static String OUTPUT_DELIMITER = "\t";

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
        String name = "Entity_" + entity.getName().replace(':', '_');
        DLDeclTerm term = new DLDeclTerm("euid", DLType.SYMBOL);
        return new DLRuleDecl(name, term);
    }

    /**
     * Extension for output files
     */
    public static final String OUTPUT_EXT = ".csv";

    /**
     * Prefix for datalog-level policy relations
     */
    final static String PolicyPrefix = "Policy_";

    /**
     * Get a declaration for a single policy rule
     */
    public static DLRuleDecl makePolicyRuleDecl(int index) {
        return makePolicyRuleDecl(PolicyPrefix + index);
    }

    /**
     * Prefix for datalog-level invariant relations
     */
    final static String InvariantPrefix = "Invariant_";

    /**
     * Get a declaration for an invariant
     */
    public static DLRuleDecl makeInvariantRuleDecl(Invariant invariant, int index) {
        String name = InvariantPrefix + index;
        List<DLDeclTerm> terms = invariant.getQuantifier().getVariables().stream()
                .map(v -> new DLDeclTerm(v.name(), DLType.SYMBOL))
                .toList();
        return new DLRuleDecl(name, terms);
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
    public static String TmpRecordType =
            "RSVP::DL::Tmp::Record::Attr::Type";

    /**
     * Generate a random EUID based on the {@link TranslationConstants#TmpRecordType}
     */
    public static EntityReference getRandomTmpEUID() {
        return new EntityReference(TmpRecordType, UUID.randomUUID().toString());
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
    public static EntityReference getUndefinedEUID(TranslationEntityDefinition def) {
        return new EntityReference(def.getEntityDefinition().getName(), UndefinedEntityUIDName);
    }

    /**
     * Get an unknown entity (UID) only. Abstraction over entities that can be given for
     * authorisation but are not in the provided list of entities
     */
    public static EntityUID getUndefinedEUID(EntityTypeDefinition def) {
        return getEUID(def, UndefinedEntityUIDName);
    }

}
