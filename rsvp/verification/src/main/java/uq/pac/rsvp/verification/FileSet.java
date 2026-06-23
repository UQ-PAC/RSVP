/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.verification;

import uq.pac.rsvp.policy.ast.entity.Entity;
import uq.pac.rsvp.policy.ast.entity.EntitySet;
import uq.pac.rsvp.policy.ast.policy.PolicyParser;
import uq.pac.rsvp.policy.ast.policy.PolicyProgram;
import uq.pac.rsvp.policy.ast.policy.PolicyStatement;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.statement.SchemaStatement;
import uq.pac.rsvp.support.util.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FileSet {

    public static final String LATEST = "latest";
    // Paths mapped to file IDs
    private final Set<List<Pair<String, Path>>> policyFiles;
    private final Map<String, Path> schemaFiles;
    private final Map<String, Path> entityFiles;
    private final Map<String, Path> invariantFiles;

    // File contents (for cedar validation)
    private final Map<String, String> policyFileContent;
    private final Map<String, String> schemaFileContent;
    private final Map<String, String> entityFileContent;

    // Parsed files
    private final Map<String, Collection<PolicyStatement>> policies;
    private final Map<String, Collection<SchemaStatement>> schemas;
    private final Map<String, Set<Entity>> entities;
    private final Map<String, Collection<PolicyStatement>> invariants;

    public FileSet() {
        // Paths mapped to file IDs
        policyFiles = new HashSet<>();
        schemaFiles = new HashMap<>();
        entityFiles = new HashMap<>();
        invariantFiles = new HashMap<>();

        // File contents
        policyFileContent = new HashMap<>();
        schemaFileContent = new HashMap<>();
        entityFileContent = new HashMap<>();

        // Parsed files
        policies = new HashMap<>();
        schemas = new HashMap<>();
        entities = new HashMap<>();
        invariants = new HashMap<>();
    }

    public FileSet loadFiles() throws IOException, IllegalAccessException {
        // Load policies
        for (List<Pair<String, Path>> versionedPolicy : policyFiles) {
            for (Pair<String, Path> policyFile : versionedPolicy) {
                String id = policyFile.getKey();
                Path location = policyFile.getValue();

                String content = Files.readString(location);
                policyFileContent.put(id, content);
                policies.put(id, PolicyParser.parse(id, content));
            }
        }

        // Load schemas
        for (Map.Entry<String, Path> schemaFile : schemaFiles.entrySet()) {
            String id = schemaFile.getKey();
            Path location = schemaFile.getValue();

            String content = Files.readString(location);
            schemaFileContent.put(id, content);
            schemas.put(id, Schema.parse(id, content).statements().toList());
        }

        // Load entities
        for (Map.Entry<String, Path> entityFile : entityFiles.entrySet()) {
            String id = entityFile.getKey();
            Path location = entityFile.getValue();

            String content = Files.readString(location);
            entityFileContent.put(id, content);
            entities.put(id, EntitySet.parse(id, content).getEntities());
        }

        // Load invariants
        for (Map.Entry<String, Path> invariantsFile : invariantFiles.entrySet()) {
            String id = invariantsFile.getKey();
            Path location = invariantsFile.getValue();

            invariants.put(id, PolicyParser.parse(id, Files.readString(location)));
        }

        return this;
    }

    public boolean noPolicies() {
        return policyFiles.isEmpty() || policyFiles.stream().allMatch(List::isEmpty);
    }

    public FileSet addPolicies(Path location) {
        return addPolicies(location.toString(), location);
    }

    public FileSet addPolicies(String filename, Path location) {
        List<Pair<String, Path>> versionList = new ArrayList<>();
        versionList.add(new Pair<>(filename, location));
        policyFiles.add(versionList);
        return this;
    }

    public FileSet addPolicies(Path... versions) {
        policyFiles.add(Arrays.stream(versions).map(path -> new Pair<>(path.toString(), path)).toList());
        return this;
    }

    @SafeVarargs
    public final FileSet addPolicies(Pair<String, Path>... versions) {
        policyFiles.add(Arrays.asList(versions));
        return this;
    }

    public String getPolicyString() {
        return getPolicyString(LATEST);
    }

    public String getPolicyString(String version) {
        return String.join("\n", getFilenameSetForVersion(version).stream().map(policyFileContent::get).toList());
    }

    public List<PolicyStatement> getPolicyStatements() {
        return getPolicyStatements(LATEST);
    }

    public List<PolicyStatement> getPolicyStatements(String version) {
        return getFilenameSetForVersion(version).stream().map(policies::get).flatMap(Collection::stream).toList();
    }

    public boolean noSchemas() {
        return schemaFiles.isEmpty();
    }

    public FileSet addSchema(Path location) {
        addSchema(location.toString(), location);
        return this;
    }

    public FileSet addSchema(String filename, Path location) {
        schemaFiles.put(filename, location);
        return this;
    }

    public String getSchemaString() {
        return String.join("\n", schemaFileContent.values());
    }

    public Collection<SchemaStatement> getSchemaStatements() {
        return schemas.values().stream().flatMap(Collection::stream).toList();
    }

    public boolean noEntities() {
        return entityFiles.isEmpty();
    }

    public FileSet addEntities(Path location) {
        addEntities(location.toString(), location);
        return this;
    }

    public FileSet addEntities(String filename, Path location) {
        entityFiles.put(filename, location);
        return this;
    }

    public List<String> getEntitiesStrings() {
        return entityFileContent.values().stream().toList();
    }

    public Set<Entity> getEntities() {
        return entities.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }

    public FileSet addInvariants(Path location) {
        addInvariants(location.toString(), location);
        return this;
    }

    public FileSet addInvariants(String filename, Path location) {
        invariantFiles.put(filename, location);
        return this;
    }

    public List<PolicyStatement> getInvariants() {
        return invariants.values().stream().flatMap(Collection::stream).toList();
    }

    public PolicyProgram getPolicyProgram() {
        return getPolicyProgram(LATEST);
    }

    public PolicyProgram getPolicyProgram(String version) {
        List<PolicyStatement> all = new ArrayList<>(getPolicyStatements(version));
        invariants.values().forEach(all::addAll);
        return PolicyProgram.of(all);
    }

    public List<Pair<String, String>> getVersionPairs() {

        List<Pair<String, String>> result = new ArrayList<>();

        for (List<Pair<String, Path>> policyFile : policyFiles) {
            for (int i = 0; i < policyFile.size() - 1; i++) {
                for (int j = i + 1; j < policyFile.size() - 1; j++) {
                    result.add(new Pair<>(policyFile.get(i).getKey(), policyFile.get(j).getKey()));
                }
                result.add(new Pair<>(policyFile.get(i).getKey(), LATEST));
            }
        }

        return result;
    }

    public boolean isLatest(String version) {
        return policyFiles.stream().anyMatch(versionedPolicy -> versionedPolicy.getLast().getKey().equals(version));
    }

    private Set<String> getFilenameSetForVersion(String version) {
        return policyFiles.stream().map(versionedPolicy -> {
            if (versionedPolicy.size() == 1) {
                // Only one version exists, return it
                return versionedPolicy.getFirst().getKey();
            } else if (version.equals(LATEST) || versionedPolicy.stream().noneMatch(policy -> policy.getKey().equals(version))) {
                // Return the latest version because it was requested or because we are filtering for
                // a different file
                return versionedPolicy.getLast().getKey();
            } else {
                return version;
            }
        }).collect(Collectors.toUnmodifiableSet());
    }
}
