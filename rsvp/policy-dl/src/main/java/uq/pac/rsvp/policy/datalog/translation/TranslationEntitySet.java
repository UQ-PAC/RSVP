package uq.pac.rsvp.policy.datalog.translation;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import uq.pac.rsvp.policy.ast.entity.EntitySet;
import uq.pac.rsvp.policy.datalog.ast.DLFact;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Translating a collection of entities into a set of datalog facts
 */
public class TranslationEntitySet {
    private final List<TranslationEntity> entities;

    public TranslationEntitySet(EntitySet entities, TranslationSchema schema) {
        List<TranslationEntity> entityList = entities.getEntities()
                .stream()
                .map(e -> new TranslationEntity(e, schema))
                .collect(Collectors.toCollection(ArrayList::new));

        // Generate undefined (UID-only) entities omitting Enum entities
        // that have pre-defined names
        for (TranslationEntityDefinition def : schema.getDefinitions()) {
            if (def.getEntityDefinition().getEntityNamesEnum().isEmpty()) {
                entityList.add(new TranslationEntity(def, TranslationConstants.getUndefinedEUID(def)));
            }
        }

        this.entities = List.copyOf(entityList);
    }

    public Multimap<String, DLFact> getFacts() {
        Multimap<String, DLFact> facts = HashMultimap.create();
        for (TranslationEntity entity : entities) {
            for (DLFact fact : entity.getFacts()) {
                facts.put(fact.getAtom().getName(), fact);
            }
        }
        return facts;
    }
}
