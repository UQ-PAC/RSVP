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

/**
 * Translation of a Cedar entity definition provided via Cedar schema to Datalog
 * <p>
 * An entity definition translates into two types of rule declarations
 * <ul>
 *     <li> Entity relation: A unary relation named after the type of the entity that tracks
 *          entities of a given type </li>
 *      <li>
 *          Attribute relations: A collection of binary relations that map entities to their attributes
 *      </li>
 * </ul>
 * For example, a schema entity definition
 * <pre>
 * entity Account = {
 *     name: String,
 *     age: Long
 * };
 * </pre>
 * Translates to
 * <pre>
 * .decl Account(x: symbol)
 * .decl Account_attr_name(x: symbol, y: symbol)
 * .decl Account_attr_agr(x: symbol, y: number)
 * </pre>
 */
public class TranslationEntityDefinition {
    /**
     * Fully-qualified entity type name
     */
    private final String name;
    /**
     * The underlying schema definition
     */
    private final EntityTypeDefinition entity;
    /**
     * Entity relation
     */
    private final DLRuleDecl relation;
    /**
     * Named attribute relations. This map associates property names to
     * translation attributes that capture details of attribute relations
     */
    private final Map<String, TranslationAttribute> attributes;

    public TranslationEntityDefinition(EntityTypeDefinition entity) {
        this.entity = entity;
        this.name = entity.getName();
        this.relation = TranslationConstants.getEntityRuleDecl(entity);
        this.attributes = new HashMap<>();

        entity.getShapeAttributeNames().forEach(attrName -> {
            CommonTypeDefinition attrType = entity.getShapeAttributeType(attrName);
            DLType dlAttrType = switch (attrType) {
                case BooleanType s -> DLType.SYMBOL;
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
                    new DLRuleDecl(relation.getName() + "_attr_" + attrName, DLType.SYMBOL, dlAttrType);
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
