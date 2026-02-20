package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.model.entity.Entity;
import com.cedarpolicy.value.*;
import uq.pac.rsvp.policy.datalog.ast.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static uq.pac.rsvp.policy.datalog.util.Assertion.require;

/**
 * Translation involving:
 *  - declaration of unary entity relations
 *  - declarations of binary attribute relations
 *
 *  FIXME: This information needs to be collected from the schema, not entities
 */
public class TranslationEntity extends Translator {

    private final List<DLStatement> statements;

    public static DLTerm getEUIDLiteral(Entity entity) {
        return getEUIDLiteral(entity.getEUID());
    }

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
            case PrimLong l -> terms.add(DLTerm.lit(l.toString()));
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

    public TranslationEntity(Entity entity, TranslationSchema schema) {
        List<DLStatement> statements = new ArrayList<>();
        TranslationType type = schema.getTranslationType(entity.getEUID().getType());
        // We assume the inputs are validated, so by the time we get to see entities,
        // it has been made sure that there is an underlying type for each encountered EUID
        require(type != null, "Cannot locate type for entity " + entity.getEUID());
        // Build facts
        DLRelationDecl relation = type.getEntityRelation();
        DLTerm euid = getEUIDLiteral(entity.getEUID());
        statements.add(new DLFact(relation.getName(), euid));

        entity.attrs.forEach((attr, value) -> {
            List<DLTerm> terms = getTerms(value);
            terms.forEach(term -> {
                DLRelationDecl ad = type.getAttribute(attr).getRelationDecl();
                statements.add(new DLFact(ad.getName(), euid, term));
            });
        });

        this.statements = Collections.unmodifiableList(statements);
    }

    @Override
    public List<DLStatement> getTranslation() {
        return statements;
    }

    @Override
    public String toString() {
        return new DLProgram(statements).toString();
    }
}
