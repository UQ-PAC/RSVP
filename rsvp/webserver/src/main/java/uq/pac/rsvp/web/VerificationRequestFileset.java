package uq.pac.rsvp.web;

import uq.pac.rsvp.verification.FileSet;
import uq.pac.rsvp.support.util.Pair;
import uq.pac.rsvp.web.service.FileService;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.IntFunction;

public record VerificationRequestFileset(Set<List<String>> policies, Set<String> schemas, Set<String> entities,
                                         Set<String> invariants) {


    public FileSet resolve(FileService fileService) {
        FileSet fileset = new FileSet();

        policies.forEach(versioned ->
                fileset.addPolicies(versioned.stream().map(
                        version -> new Pair<>(version, fileService.getPath(version))
                ).toArray(new PairArray()))
        );
        schemas.forEach(schema -> fileset.addSchema(schema, fileService.getPath(schema)));
        entities.forEach(entities -> fileset.addEntities(entities, fileService.getPath(entities)));
        invariants.forEach(invariants -> fileset.addInvariants(invariants, fileService.getPath(invariants)));

        return fileset;
    }

    private static class PairArray implements IntFunction<Pair<String, Path>[]> {
        @Override
        @SuppressWarnings("unchecked")
        public Pair<String, Path>[] apply(int size) {
            return (Pair<String, Path>[]) new Pair[size];
        }
    }
}
