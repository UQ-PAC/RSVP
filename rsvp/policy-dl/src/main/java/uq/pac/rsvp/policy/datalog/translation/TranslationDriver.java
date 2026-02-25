package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.model.entity.Entities;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.datalog.ast.DLProgram;

import java.util.*;

/**
 * Putting translation of the cedar schema, entries, context and policies together
 */
public class TranslationDriver {
    public static DLProgram getTranslation(Schema schema, Entities entities) {
        TranslationSchema translationSchema = new TranslationSchema(schema);
        List<TranslationEntity> facts = entities.getEntities()
                .stream()
                .map(e -> new TranslationEntity(e, translationSchema))
                .toList();
        DLProgram.Builder builder = new DLProgram.Builder();
        translationSchema.getTranslationTypes().forEach(t -> builder.add(t.getTranslation()));
        facts.forEach(t -> builder.add(t.getTranslation()));
        return builder.build();
    }
}
