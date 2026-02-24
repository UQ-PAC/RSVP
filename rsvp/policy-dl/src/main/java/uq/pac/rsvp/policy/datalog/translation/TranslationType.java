package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.value.*;
import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.EntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.common.*;
import uq.pac.rsvp.policy.datalog.ast.DLProgram;
import uq.pac.rsvp.policy.datalog.ast.DLRelationDecl;
import uq.pac.rsvp.policy.datalog.ast.DLStatement;
import uq.pac.rsvp.policy.datalog.ast.DLType;
import uq.pac.rsvp.policy.datalog.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranslationType extends Translator {
    private final String name;
    private final EntityTypeDefinition entity;
    private final DLRelationDecl relation;
    private final Map<String, TranslationAttribute> attributeRelations;

    public TranslationType(EntityTypeDefinition entity) {
        this.entity = entity;
        this.name = entity.getName();
        String relationName = name.replace(':', '_');
        this.relation = new DLRelationDecl(relationName, DLType.SYMBOL);
        this.attributeRelations = new HashMap<>();


        entity.getShapeAttributeNames().forEach(attrName -> {
            CommonTypeDefinition attrType = entity.getShapeAttributeType(attrName);
            DLType dlAttrType = switch (attrType) {
                case StringType s -> DLType.SYMBOL;
                case LongType s -> DLType.NUMBER;
                case EntityTypeReference r -> DLType.SYMBOL;
                case SetTypeDefinition s -> {
                    CommonTypeDefinition sub = s.getElementType();
                    if (Util.instanceOf(s.getElementType(), LongType.class, StringType.class, EntityTypeReference.class)) {
                        yield DLType.SYMBOL;
                    } else {
                        throw new RuntimeException("Unsupported set type: " + sub.getClass().getSimpleName());
                    }
                }
                default -> throw new RuntimeException("Unsupported type: "
                        + attrType.getClass().getSimpleName());
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

    public EntityTypeDefinition getEntityDefinition() {
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
