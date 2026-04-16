package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.entity.*;
import uq.pac.rsvp.policy.ast.schema.EntityTypeDefinition;
import uq.pac.rsvp.policy.datalog.ast.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.AttributeRuleDecl;
import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.HasAttributeRuleDecl;
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

    private final RecordMap records;

    private void addAttributeFacts(EntityValue value, DLTerm euid, String attr, List<DLFact> statements) {
        Consumer<DLTerm> addFact = vt ->
                statements.add(new DLFact(AttributeRuleDecl, euid, DLTerm.lit(attr), vt));

        statements.add(new DLFact(HasAttributeRuleDecl, euid, DLTerm.lit(attr)));

        switch (value) {
            case StringValue s -> addFact.accept(DLTerm.lit(s.toString()));
            case LongValue l -> addFact.accept(DLTerm.lit(Long.toString(l.getValue())));
            case BooleanValue b -> addFact.accept(DLTerm.lit(b.toString()));
            case EntityReference e -> addFact.accept(DLTerm.lit(e.getReference()));
            case SetValue lst -> lst.forEach(v -> addAttributeFacts(v, euid, attr, statements));
            case RecordValue map -> {
                EntityReference recEuid = records.getReference(map);
                DLTerm recTerm = DLTerm.lit(recEuid.getReference());
                addAttributeFacts(recEuid, euid, attr, statements);
                map.forEach((at, vl) -> addAttributeFacts(vl, recTerm, at.getValue(), statements));
            }
            default -> throw new TranslationError("Unsupported value: " + value.getClass());
        }
    }

    /**
     * A special constructor that generates only entity relation.
     * This is for the case when only an entity UID is known, but
     * not the entire entity
     */
    public TranslationEntity(TranslationEntityDefinition definition, EntityReference uid) {
        this.definition = definition;
        DLRuleDecl relation = definition.getEntityRuleDecl();
        DLTerm euid = DLTerm.lit(uid.getReference());
        this.facts = List.of(new DLFact(relation, euid));
        this.records = null;
    }

    public TranslationEntity(Entity entity, TranslationSchema schema, RecordMap records) {
        this.records = records;
        List<DLFact> statements = new ArrayList<>();
        this.definition = schema.getTranslationEntityType(entity.getEuid().getType());
        // We assume the inputs are validated, so by the time we get to see entities,
        // it has been made sure that there is an underlying type for each encountered EUID
        require(definition != null, "Cannot locate type for entity " + entity.getEuid());
        // Build facts
        DLRuleDecl decl = definition.getEntityRuleDecl();
        DLTerm euid = DLTerm.lit(entity.getEuid().getReference());
        statements.add(new DLFact(decl, euid));

        entity.getAttrs().forEach((attr, value) -> {
            addAttributeFacts(value, DLTerm.lit(entity.getEuid().getReference()), attr.getValue(), statements);
        });

		// Generate facts for parent hierarchy
        entity.getParents().forEach(pid -> {
            statements.add(new DLFact(TranslationConstants.ParentOfRuleDecl, DLTerm.lit(pid.getReference()), euid));
        });
        // Entity is reflexive, an element is a member of its own hierarchy
        statements.add(new DLFact(TranslationConstants.ParentOfRuleDecl, euid, euid));

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
}
