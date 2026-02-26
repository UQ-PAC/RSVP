package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.model.entity.Entities;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import uq.pac.rsvp.policy.datalog.ast.DLFact;

import java.util.*;

/**
 * Translating a collection of entities into a set of datalog facts
 */
public class TranslationEntitySet {
    private final List<TranslationEntity> entities;

    public TranslationEntitySet(Entities entities, TranslationSchema schema) {
        this.entities = entities.getEntities()
                .stream()
                .map(e -> new TranslationEntity(e, schema))
                .toList();
    }

    public List<TranslationEntity> getTranslationEntities() {
        return entities;
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
