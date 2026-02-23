package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.value.*;
import uq.pac.rsvp.policy.ast.schema.EntityType;
import uq.pac.rsvp.policy.ast.schema.Namespace;
import uq.pac.rsvp.policy.ast.schema.attribute.*;
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
public class TranslationType extends Translator {
    // FIXME: Entity Definition
    private final String name;
    private final EntityType entity;
    private final DLRelationDecl relation;
    private final Map<String, TranslationAttribute> attributeRelations;

    public TranslationType(EntityType entity, Namespace namespace) {
        this.entity = entity;
        String prefix = namespace.getName().isEmpty() ? "" : namespace.getName() + "::";
        this.name = prefix + entity.getName();
        String relationName = name.replace(':', '_');
        this.relation = new DLRelationDecl(relationName, DLType.SYMBOL);
        this.attributeRelations = new HashMap<>();

        RecordType shape = entity.getShape();
        shape.getAttributeNames().forEach(attrName -> {
            AttributeType attrType = shape.getAttributeType(attrName);
            DLType dlAttrType = switch (attrType) {
                case PrimitiveType s ->
                        s.getType() == PrimitiveType.Type.Long ?  DLType.NUMBER : DLType.SYMBOL;
                case EntityOrCommonType i -> DLType.SYMBOL;
                case SetType s -> {
                    Class<?> sub = s.getElementType().getClass();
                    if (sub != PrimitiveType.class && sub != EntityOrCommonType.class) {
                        throw new RuntimeException("Unsupported set type: " + s);
                    }
                    yield DLType.SYMBOL;
                }
                default -> throw new RuntimeException("Unsupported type: " + attrType.getClass().getSimpleName());
            };

            DLRelationDecl dlAttributeRelation =
                    new DLRelationDecl(relationName + "_attr_" + attrName, DLType.SYMBOL, dlAttrType);
            attributeRelations.put(attrName, new TranslationAttribute(attrName, attrType, dlAttributeRelation));
        });
    }

    public DLRelationDecl getEntityRelation() {
        return relation;
    }

    public TranslationAttribute getAttribute(String attr) {
        return attributeRelations.get(attr);
    }

    public EntityType getEntityDefinition() {
        return entity;
    }

    public List<DLStatement> getTranslation() {
        List<DLStatement> statements = new ArrayList<>();
        statements.add(relation);
        statements.addAll(attributeRelations.values().stream()
                .map(TranslationAttribute::getRelationDecl)
                .toList());
        return statements;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return new DLProgram(getTranslation()).toString();
    }
}
