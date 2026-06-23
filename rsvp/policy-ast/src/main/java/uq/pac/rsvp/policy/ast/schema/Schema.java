/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.ast.schema;

import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;
import uq.pac.rsvp.policy.ast.schema.parser.SchemaParser;
import uq.pac.rsvp.policy.ast.schema.parser.StatementResolutionVisitor;
import uq.pac.rsvp.policy.ast.schema.statement.ActionDefinition;
import uq.pac.rsvp.policy.ast.schema.statement.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.statement.EntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.statement.EnumEntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.statement.RecordEntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.statement.SchemaStatement;
import uq.pac.rsvp.policy.ast.schema.type.*;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaVisitorAdapter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class Schema {

    private final Map<TypeReference, SchemaStatement> statements;

    // Private constructor for internal purposes
    private Schema(Map<TypeReference, SchemaStatement> statements) {
        this.statements = Collections.unmodifiableMap(statements);
    }

    public SchemaStatement get(TypeReference ref) {
        return statements.get(ref);
    }

    public Stream<SchemaStatement> statements() {
        return statements.values().stream();
    }

    public static Schema parse(String file, String text) {
        return SchemaParser.parse(file, text);
    }

    public static Schema parse(Path file) throws IOException {
        return SchemaParser.parse(file.toString(), Files.readString(file));
    }

    private <E extends SchemaStatement> Stream<E> statementStream(Class<E> target) {
        return statements.values().stream()
                .filter(target::isInstance)
                .map(target::cast);
    }

    public Stream<EntityTypeDefinition> entityTypes() {
        return statementStream(EntityTypeDefinition.class);
    }

    public Stream<EnumEntityTypeDefinition> enumEntityTypes() {
        return statementStream(EnumEntityTypeDefinition.class);
    }

    public Stream<RecordEntityTypeDefinition> recordEntityTypes() {
        return statementStream(RecordEntityTypeDefinition.class);
    }

    public Stream<ActionDefinition> actions() {
        return statementStream(ActionDefinition.class);
    }

    public Stream<CommonTypeDefinition> types() {
        return statementStream(CommonTypeDefinition.class);
    }

    private <E extends SchemaStatement> E getTypedStatement(TypeReference reference, Class<E> target) {
        SchemaStatement stmt = statements.get(reference);
        if (target.isInstance(stmt)) {
            return target.cast(stmt);
        }
        return null;
    }

    public SchemaStatement getStatement(TypeReference ref) {
        return statements.get(ref);
    }

    public EntityTypeDefinition getEntityType(TypeReference ref) {
        return getTypedStatement(ref, EntityTypeDefinition.class);
    }

    public RecordEntityTypeDefinition getRecordEntityType(TypeReference ref) {
        return getTypedStatement(ref, RecordEntityTypeDefinition.class);
    }

    public EnumEntityTypeDefinition getEnumEntityType(TypeReference ref) {
        return getTypedStatement(ref, EnumEntityTypeDefinition.class);
    }

    public CommonTypeDefinition getCommonType(TypeReference ref) {
        return getTypedStatement(ref, CommonTypeDefinition.class);
    }

    public ActionDefinition getAction(TypeReference ref) {
        return getTypedStatement(ref, ActionDefinition.class);
    }

    /**
     * Check whether a reference corresponds to an actual construct within a schema
     */
    public boolean isValid(TypeReference reference) {
        return statements.containsKey(reference);
    }

    /**
     * String representation of the schema. The toString representation generates a parseable
     * schema, such that parsing the string representation generates an identical schema.
     * The string representation is also stable and enforces lexicographical order of schema
     * statements
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // Generate initial associations mapping namespaces to the statements they contain
        Map<String, List<SchemaStatement>> namespaces = new HashMap<>();
        statements.values().forEach(v -> {
            namespaces.computeIfAbsent(v.getNamespace(), k -> new ArrayList<>()).add(v);
        });

        // Sort the map
        Map<String, List<SchemaStatement>> sortedNamespaces = new LinkedHashMap<>();
        namespaces.keySet().stream().sorted()
                .forEach(ns -> sortedNamespaces.put(ns, new ArrayList<>()));

        for (String ns : namespaces.keySet()) {
            List<SchemaStatement> statements = namespaces.get(ns);
            List<SchemaStatement> sortedStatements =  statements.stream()
                    .sorted(Comparator.comparing(Object::toString))
                    .toList()
                    .reversed();
            sortedNamespaces.get(ns).addAll(sortedStatements);
        }

        sortedNamespaces.forEach((ns, stmts) -> {
            if (!ns.isEmpty()) {
                sb.append("namespace ").append(ns).append(" { ").append("\n");
            }
            stmts.forEach(stmt -> {
                String prefix = ns.isEmpty() ? "" : "    ";
                Arrays.stream(stmt.toString().split("\\n")).forEach(line -> {
                    sb.append(prefix).append(line).append("\n");
                });
            });
            if (!ns.isEmpty()) {
                sb.append("}").append("\n");
            }
        });
        return sb.toString();
    }

    // Build and validate Cedar schema
    public static Schema of(Collection<SchemaStatement> statements) {
        // Ensure no illegal shadowing
        Schema result = uniquenessPass(statements);
        result = shallowResolutionPass(result);
        result = typeDependencyPass(result);
        return result;
    }

    // The first step of schema generation.
    // The resulting schema contains no duplicates (i.e., illegally shadowed statements)
    // This includes similarly named statements within the same namespace and conflicts with
    // global
    private static Schema uniquenessPass(Collection<SchemaStatement> statements) {
        // Sort the list of statements such that entities from the global namespace come first
        statements = statements.stream()
                .sorted(Comparator.comparingInt(a -> a.getNamespace().length()))
                .toList();

        Map<TypeReference, SchemaStatement> types = new HashMap<>();
        for (SchemaStatement stmt : statements) {
            TypeReference ref = stmt.getTypeReference();
            // Try and lookup the reference by name or by basename in case it exists in the global namespace
            SchemaStatement lookup = types.containsKey(ref) ?
                    types.get(ref) : types.get(new TypeReference("", ref.getBaseName()));
            if (lookup != null) {
                throw new SchemaResolutionException("Reference %s illegally shadowed"
                        .formatted(lookup.getTypeReference()));
            }
            types.put(ref, stmt);
        }

        return new Schema(types);
    }

    // The second stage of resolution.
    // - Ensure all references are valid
    // - Resolve references to built-in primitive types (references to common types persist)
    private static Schema shallowResolutionPass(Schema schema) {
        Map<TypeReference, SchemaStatement> resolved = new HashMap<>();
        schema.statements.forEach((ref, stmt) -> {
            StatementResolutionVisitor resolver =
                    new StatementResolutionVisitor(schema, stmt.getNamespace());
            resolved.put(ref, stmt.compute(resolver));
        });
        return new Schema(resolved);
    }

    // The third stage of resolution:
    // ensure that definition of common types are not recursive
    private static Schema typeDependencyPass(Schema schema) {
        MutableGraph<TypeReference> typeGraphBuilder = GraphBuilder
                .directed()
                .allowsSelfLoops(true)
                .build();

        schema.statements()
                .map(s -> s instanceof CommonTypeDefinition t ? t : null)
                .filter(Objects::nonNull)
                .forEach(c -> {
                    typeGraphBuilder.addNode(c.getTypeReference());
                    c.getDefinition().accept(new SchemaVisitorAdapter() {
                        @Override
                        public void visitTypeReference(TypeReference type) {
                            SchemaStatement stmt = requireNonNull(schema.get(type));
                            if (stmt instanceof CommonTypeDefinition ct) {
                                typeGraphBuilder.putEdge(c.getTypeReference(), ct.getTypeReference());
                            }
                        }
                    });
                });

        // First check for self-recursion, as later on after building the transitive closure
        // we cannot distinguish from initial edges or edges built by transitive closure
        for (TypeReference node : typeGraphBuilder.nodes()) {
            if (typeGraphBuilder.successors(node).contains(node)) {
                throw new SchemaResolutionException("Recursive definition of type: " + node);
            }
        }

        // Build transitive closure for cycle detection
        Graph<TypeReference> typeGraph =
                Graphs.transitiveClosure(typeGraphBuilder,
                        Graphs.TransitiveClosureSelfLoopStrategy.ADD_SELF_LOOPS_FOR_CYCLES);

        for (TypeReference src : typeGraph.nodes()) {
            for (TypeReference dest : typeGraph.nodes()) {
                if (typeGraph.hasEdgeConnecting(src, dest) && typeGraph.hasEdgeConnecting(dest, src)) {
                    if (!src.equals(dest)) {
                        throw new SchemaResolutionException(
                                "Mutually recursive definition of types: %s, %s".formatted(src, dest));
                    }
                }
            }
        }

        return schema;
    }

    // Rewrite a built-in type into one containing no common types according to the schema
    private static BuiltinType resolve(Schema schema, BuiltinType shape) {
        return switch (shape) {
            case SetType s ->
                    new SetType(resolve(schema, s.getElementType()), s.getSourceLoc());
            case RecordType r -> {
                Map<RecordType.Attribute, BuiltinType> attributes = new HashMap<>();
                r.getAttributes().forEach((attr, type) -> {
                    attributes.put(attr, resolve(schema, type));
                });
                yield new RecordType(attributes, r.getSourceLoc());
            }
            case TypeReference t -> {
                SchemaStatement stmt = schema.get(t);
                if (stmt instanceof CommonTypeDefinition common) {
                    yield resolve(schema, common.getDefinition());
                }
                yield shape;
            }
            default -> shape;
        };
    }

    // Generate a schema that resolves, in-lines and removes common type definitions
    public Schema resolveCommonTypes() {
        Map<TypeReference, SchemaStatement> resolved = new HashMap<>();
        for (Map.Entry<TypeReference, SchemaStatement> entry : statements.entrySet()) {
            SchemaStatement stmt = entry.getValue();
            if (stmt instanceof RecordEntityTypeDefinition rd) {
                RecordType shape = (RecordType) resolve(this, rd.getShape());
                stmt = new RecordEntityTypeDefinition(
                        rd.getTypeReference(), rd.getMemberOf(), shape,
                        rd.getAnnotations(), rd.getSourceLoc());
            }

            if (!(stmt instanceof CommonTypeDefinition)) {
                resolved.put(entry.getKey(), stmt);
            }
        }
        return new Schema(resolved);
    }

    /**
     * Return {@code true} if this schema has no unresolved types
     */
    public boolean isResolved() {
        return types().findAny().isEmpty();
    }
}
