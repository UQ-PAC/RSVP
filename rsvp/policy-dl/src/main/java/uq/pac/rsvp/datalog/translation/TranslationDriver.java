package uq.pac.rsvp.datalog.translation;

import com.cedarpolicy.model.entity.Entities;
import com.cedarpolicy.model.exception.AuthException;
import uq.pac.rsvp.datalog.ast.DLProgram;
import uq.pac.rsvp.datalog.util.Util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class TranslationDriver {

    public static DLProgram getTranslation(Entities entities) {
        List<TranslationType> types = entities.getEntities()
                .stream()
                .filter(Util.distinctBy(k -> k.getEUID().getType()))
                .map(TranslationType::new)
                .toList();

        TranslationSchema schema = new TranslationSchema(types);

        List<TranslationEntity> facts = entities.getEntities()
                .stream()
                .map(e -> new TranslationEntity(e, schema))
                .toList();

        DLProgram.Builder builder = new DLProgram.Builder();
        types.forEach(t -> builder.add(t.getTranslation()));
        facts.forEach(t -> builder.add(t.getTranslation()));

        return builder.build();
    }
}
