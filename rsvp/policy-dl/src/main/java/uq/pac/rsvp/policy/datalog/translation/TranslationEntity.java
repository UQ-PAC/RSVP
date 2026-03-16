package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.model.entity.Entity;
import com.cedarpolicy.value.*;
import uq.pac.rsvp.policy.ast.schema.EntityTypeDefinition;
import uq.pac.rsvp.policy.datalog.ast.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.AttributeRuleDecl;
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

    public static String getEUIDString(EntityUID id) {
        return id.toCedarExpr();
    }

    public static DLTerm getEUIDLiteral(EntityUID id) {
        return DLTerm.lit(getEUIDString(id));
    }

    private void addAttributeFacts(Value value, DLTerm euid, String attr, List<DLFact> statements) {
        Consumer<DLTerm> addFact = vt ->
                statements.add(new DLFact(AttributeRuleDecl, euid, DLTerm.lit(attr), vt));

        switch (value) {
            case PrimString s -> addFact.accept(DLTerm.lit(s.toString()));
            case PrimLong l -> addFact.accept(DLTerm.lit(Long.toString(l.getValue())));
            case PrimBool b -> addFact.accept(DLTerm.lit(b.toString()));
            case EntityUID e -> addFact.accept(DLTerm.lit(getEUIDString(e)));
            case CedarList lst -> lst.forEach(v -> addAttributeFacts(v, euid, attr, statements));
            case CedarMap map -> {
                EntityUID recEuid = Naming.getEUID();
                DLTerm recTerm = getEUIDLiteral(recEuid);
                addAttributeFacts(recEuid, euid, attr, statements);
                map.forEach((at, vl) -> addAttributeFacts(vl, recTerm, at, statements));
            }
            default -> throw new TranslationError("Unsupported value: " + value.getClass());
        }
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
            addAttributeFacts(value, getEUIDLiteral(entity.getEUID()), attr, statements);
        });

		// Generate facts for parent hierarchy
        entity.parentsEUIDs.forEach(pid -> {
            statements.add(new DLFact(TranslationConstants.ParentOfRuleDecl, getEUIDLiteral(pid), euid));
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
