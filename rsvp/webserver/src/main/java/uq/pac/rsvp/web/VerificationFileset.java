package uq.pac.rsvp.web;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VerificationFileset {
    private final Set<List<String>> policyFiles;
    private final Set<String> schemas;
    private final Set<String> entities;
    private final Set<String> invariants;

    public VerificationFileset(Collection<List<String>> policyFiles, Collection<String> schemas,
            Collection<String> entities, Collection<String> invariants) {
        this.policyFiles = new HashSet<>();
        for (List<String> versionedfile : policyFiles) {
            this.policyFiles.add(List.copyOf(versionedfile));
        }
        this.schemas = new HashSet<>(schemas);
        this.entities = new HashSet<>(entities);
        this.invariants = new HashSet<>(invariants);
    }

    public VerificationFileset() {
        this.policyFiles = new HashSet<>();
        this.schemas = new HashSet<>();
        this.entities = new HashSet<>();
        this.invariants = new HashSet<>();

    }

    public Set<List<String>> getPolicyFiles() {
        return Set.copyOf(policyFiles);
    }

    public Set<String> getSchemas() {
        return Set.copyOf(schemas);
    }

    public Set<String> getEntities() {
        return Set.copyOf(entities);
    }

    public Set<String> getInvariants() {
        return Set.copyOf(invariants);
    }
}
