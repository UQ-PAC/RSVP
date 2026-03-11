package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.model.entity.Entity;
import com.cedarpolicy.value.*;
import uq.pac.rsvp.policy.ast.schema.EntityTypeDefinition;
import uq.pac.rsvp.policy.datalog.ast.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static uq.pac.rsvp.policy.datalog.util.Assertion.require;

/**
 * Translation of a concrete entities to Datalog. As per entity definition
 * (see {@link EntityTypeDefinition}) a concrete entity translates into a collection
 * of datalog facts for relations provided by the definition.
 */
public class TranslationEntity {
    /**
     * The generated list of facts including entity and attribute relations facts
     */
    private final List<DLFact> facts;
    /**
     * Reference of the definition of this entity
     */
    private final TranslationEntityDefinition definition;

    public static DLTerm getEUIDLiteral(EntityUID id) {
        String prefix = id.getType().toString();
        if (!prefix.isEmpty()) {
            prefix += "::";
        }
        return DLTerm.lit(prefix + id.getId().toString());
    }

    private void getTerms(Value value, List<DLTerm> terms) {
        switch (value) {
            case PrimString s -> terms.add(DLTerm.lit(s.toString()));
            case PrimLong l -> terms.add(DLTerm.lit(l.getValue()));
            case PrimBool b -> terms.add(DLTerm.lit(b.toString()));
            case EntityUID e -> terms.add(getEUIDLiteral(e));
            case CedarList l -> {
                for (Value v : l) {
                    if (v instanceof CedarList) {
                        throw new RuntimeException("Unsupported value: " + l);
                    }
                    getTerms(v, terms);
                }
            }
            default -> throw new RuntimeException("Unsupported value: " + value);
        }
    }

    private List<DLTerm> getTerms(Value value) {
        List<DLTerm> terms = new ArrayList<>();
        getTerms(value, terms);
        return terms;
    }

    /**
     * A special constructor that generates only entity relation.
     * This is for the case when only an entity UID is known, but
     * not the entire entity
     */
    public TranslationEntity(TranslationEntityDefinition definition, EntityUID uid) {
        this.definition = definition;
        DLRuleDecl relation = definition.getEntityRuleDecl();
        DLTerm euid = getEUIDLiteral(uid);
        this.facts = List.of(new DLFact(relation, euid));
    }

    public TranslationEntity(Entity entity, TranslationSchema schema) {
        List<DLFact> statements = new ArrayList<>();
        this.definition = schema.getTranslationEntityType(entity.getEUID().getType().toString());
        // We assume the inputs are validated, so by the time we get to see entities,
        // it has been made sure that there is an underlying type for each encountered EUID
        require(definition != null, "Cannot locate type for entity " + entity.getEUID());
        // Build facts
        DLRuleDecl decl = definition.getEntityRuleDecl();
        DLTerm euid = getEUIDLiteral(entity.getEUID());
        statements.add(new DLFact(decl, euid));

        entity.attrs.forEach((attr, value) -> {
            List<DLTerm> terms = getTerms(value);
            terms.forEach(term -> {
                DLRuleDecl ad = definition.getAttribute(attr).getRuleDecl();
                statements.add(new DLFact(ad, euid, term));
            });
        });

		// Generate facts for parent hierarchy
        entity.parentsEUIDs.forEach(pid -> {
            statements.add(new DLFact(TranslationConstants.ParentOfRuleDecl, getEUIDLiteral(pid), euid));
        });

        this.facts = Collections.unmodifiableList(statements);
    }

    public List<DLFact> getFacts() {
        return facts;
    }

    @Override
    public String toString() {
        List<DLStatement> statements = facts.stream()
                .map(f -> (DLStatement)f)
                .toList();
        return new DLProgram(statements).toString();
    }

    public TranslationEntityDefinition getEntityTypeDefinition() {
        return definition;
    }
}
