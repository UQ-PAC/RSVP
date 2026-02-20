package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.model.entity.Entities;
import uq.pac.rsvp.policy.datalog.ast.DLProgram;
import uq.pac.rsvp.policy.datalog.util.Util;

import java.util.*;

/**
 * Putting translation of the cedar schema, entries, context and policies together
 */
public class TranslationDriver {

    public static DLProgram getTranslation(Entities entities) {
        List<TranslationEntity> types = entities.getEntities()
                .stream()
                .filter(Util.distinctBy(k -> k.getEUID().getType()))
                .map(TranslationEntity::new)
                .toList();

        TranslationSchema schema = new TranslationSchema(types);

        List<TranslationEntitySchema> facts = entities.getEntities()
                .stream()
                .map(e -> new TranslationEntitySchema(e, schema))
                .toList();

        DLProgram.Builder builder = new DLProgram.Builder();
        types.forEach(t -> builder.add(t.getTranslation()));
        facts.forEach(t -> builder.add(t.getTranslation()));

        return builder.build();
    }
}
