package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.model.entity.Entity;
import com.cedarpolicy.value.*;
import uq.pac.rsvp.policy.datalog.ast.DLProgram;
import uq.pac.rsvp.policy.datalog.ast.DLRelationDecl;
import uq.pac.rsvp.policy.datalog.ast.DLStatement;
import uq.pac.rsvp.policy.datalog.ast.DLType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Translation of entities. For the moment each entity translates into a collection of facts
 * - A fact for the entity relation
 * - Collection of facts for attribute relations
 */
public class TranslationType extends TranslationComponent {

    private final EntityTypeName entityTypeName;
    private final DLRelationDecl relation;
    private final Map<String, DLRelationDecl> attributeRelations;

    public TranslationType(Entity entity) {
        EntityUID uid = entity.getEUID();
        String relationName = uid.getType().toString().replace(':', '_');
        this.entityTypeName = uid.getType();
        this.relation = new DLRelationDecl(relationName, DLType.SYMBOL);

        this.attributeRelations = new HashMap<>();
        entity.attrs.forEach((attr, value) -> {
            DLType attrType = switch (value) {
                case PrimString s -> DLType.SYMBOL;
                case PrimLong s -> DLType.NUMBER;
                case PrimBool b -> DLType.SYMBOL;
                case EntityUID i -> DLType.SYMBOL;
                case CedarList l -> DLType.SYMBOL;
                default -> throw new RuntimeException("Unsupported type: " + value.getClass().getSimpleName());
            };
            DLRelationDecl attributeRelation =
                     new DLRelationDecl(relationName + "_attr_" + attr, DLType.SYMBOL, attrType);
            attributeRelations.put(attr, attributeRelation);
        });
    }

    DLRelationDecl getEntityRelation() {
        return relation;
    }

    public DLRelationDecl getAttributeRelation(String attr) {
        return attributeRelations.get(attr);
    }

    public EntityTypeName getTypeName() {
        return entityTypeName;
    }

    public List<DLStatement> getTranslation() {
        List<DLStatement> statements = new ArrayList<>();
        statements.add(relation);
        statements.addAll(attributeRelations.values());
        return statements;
    }

    @Override
    public String toString() {
        return new DLProgram(getTranslation()).toString();
    }
}
