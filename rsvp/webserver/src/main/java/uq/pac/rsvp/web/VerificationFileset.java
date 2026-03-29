package uq.pac.rsvp.web;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VerificationFileset {
    private final Set<List<VersionedPolicy>> policyFiles;
    private final Set<String> schemas;
    private final Set<String> entities;
    private final Set<String> invariants;

    public static class VersionedPolicy {
        private final String version;
        private final String id;

        public VersionedPolicy(String version, String id) {
            this.version = version;
            this.id = id;
        }

        public VersionedPolicy() {
            this.version = null;
            this.id = null;
        }

        public String getVersion() {
            return version;
        }

        public String getId() {
            return id;
        }
    }

    public VerificationFileset(Collection<List<VersionedPolicy>> policyFiles, Collection<String> schemas,
            Collection<String> entities, Collection<String> invariants) {
        this.policyFiles = new HashSet<>();
        for (List<VersionedPolicy> versionedfile : policyFiles) {
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

    public Set<List<VersionedPolicy>> getPolicyFiles() {
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
