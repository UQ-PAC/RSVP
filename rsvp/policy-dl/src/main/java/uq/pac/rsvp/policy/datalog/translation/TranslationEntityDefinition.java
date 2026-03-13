package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.EntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.common.*;
import uq.pac.rsvp.policy.datalog.ast.DLProgram;
import uq.pac.rsvp.policy.datalog.ast.DLRuleDecl;
import uq.pac.rsvp.policy.datalog.ast.DLStatement;

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
     * Entity relation declaration
     */
    private final DLRuleDecl entityDecl;

    private final static Set<Class<? extends CommonTypeDefinition>> SUPPORTED_TYPES = Set.of(
            BooleanType.class,
            StringType.class,
            LongType.class,
            EntityTypeReference.class);

    private boolean isSupportedType(CommonTypeDefinition def) {
        return SUPPORTED_TYPES.contains(def.getClass()) || def instanceof SetTypeDefinition set &&
                        SUPPORTED_TYPES.contains(set.getElementType().getClass());
    }

    public TranslationEntityDefinition(EntityTypeDefinition entity) {
        this.entity = entity;
        this.name = entity.getName();
        this.entityDecl = TranslationConstants.getEntityRuleDecl(entity);

        entity.getShapeAttributeNames().forEach(attrName -> {
            CommonTypeDefinition attrType = entity.getShapeAttributeType(attrName);
            TranslationError.error(isSupportedType(attrType), "Unsupported type %s in %s.%s",
                    attrType.getName(), entity.getName(), attrName);
        });
    }

    public DLRuleDecl getEntityRuleDecl() {
        return entityDecl;
    }

    public EntityTypeDefinition getEntityDefinition() {
        return entity;
    }

    public List<DLStatement> getTranslation() {
        List<DLStatement> statements = new ArrayList<>();
        statements.add(entityDecl);
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
