package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.EntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.common.*;
import uq.pac.rsvp.policy.datalog.ast.DLProgram;
import uq.pac.rsvp.policy.datalog.ast.DLRuleDecl;
import uq.pac.rsvp.policy.datalog.ast.DLStatement;
import uq.pac.rsvp.policy.datalog.ast.DLType;
import uq.pac.rsvp.policy.datalog.util.Util;

import java.util.*;

public class TranslationEntityType {
    private final String name;
    private final EntityTypeDefinition entity;
    private final DLRuleDecl relation;
    private final Map<String, TranslationAttribute> attributes;

    public TranslationEntityType(EntityTypeDefinition entity) {
        this.entity = entity;
        this.name = entity.getName();
        String relationName = name.replace(':', '_');
        this.relation = new DLRuleDecl(relationName, DLType.SYMBOL);
        this.attributes = new HashMap<>();

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

            DLRuleDecl dlAttributeRelation =
                    new DLRuleDecl(relationName + "_attr_" + attrName, DLType.SYMBOL, dlAttrType);
            attributes.put(attrName, new TranslationAttribute(attrName, attrType, dlAttributeRelation));
        });
    }

    public DLRuleDecl getEntityRuleDecl() {
        return relation;
    }

    public TranslationAttribute getAttribute(String attr) {
        return attributes.get(attr);
    }

    public EntityTypeDefinition getEntityDefinition() {
        return entity;
    }

    Collection<TranslationAttribute> getAttributes() {
        return attributes.values();
    }

    public List<DLStatement> getTranslation() {
        List<DLStatement> statements = new ArrayList<>();
        statements.add(relation);
        statements.addAll(attributes.values().stream()
                .map(TranslationAttribute::getRuleDecl)
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
