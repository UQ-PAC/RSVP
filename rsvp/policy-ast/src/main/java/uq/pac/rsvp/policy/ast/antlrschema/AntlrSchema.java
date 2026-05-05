package uq.pac.rsvp.policy.ast.antlrschema;

import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;
import uq.pac.rsvp.policy.ast.antlrschema.parser.AntlrSchemaParser;
import uq.pac.rsvp.policy.ast.antlrschema.parser.AntlrStatementResolutionVisitor;
import uq.pac.rsvp.policy.ast.antlrschema.statement.*;
import uq.pac.rsvp.policy.ast.antlrschema.type.*;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaVisitorAdapter;
import uq.pac.rsvp.policy.ast.schema.SchemaResolutionException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class AntlrSchema {

    private final Map<AntlrTypeReference, AntlrSchemaStatement> statements;

    // Private constructor for internal purposes
    private AntlrSchema(Map<AntlrTypeReference, AntlrSchemaStatement> statements) {
        this.statements = Collections.unmodifiableMap(statements);
    }

    public AntlrSchemaStatement get(AntlrTypeReference ref) {
        return statements.get(ref);
    }

    public Stream<AntlrSchemaStatement> statements() {
        return statements.values().stream();
    }

    public static AntlrSchema parse(String file, String text) {
        return AntlrSchemaParser.parse(file, text);
    }

    public static AntlrSchema parse(Path file) throws IOException {
        return AntlrSchemaParser.parse(file.toString(), Files.readString(file));
    }

    private <E extends AntlrSchemaStatement> Stream<E> statementStream(Class<E> target) {
        return statements.values().stream()
                .filter(target::isInstance)
                .map(target::cast);
    }

    public Stream<AntlrEntityType> entityTypes() {
        return statementStream(AntlrEntityType.class);
    }

    public Stream<AntlrEnumEntityType> enumEntityTypes() {
        return statementStream(AntlrEnumEntityType.class);
    }

    public Stream<AntlrRecordEntityType> recordEntityTypes() {
        return statementStream(AntlrRecordEntityType.class);
    }

    public Stream<AntlrAction> actions() {
        return statementStream(AntlrAction.class);
    }

    public Stream<AntlrCommonType> types() {
        return statementStream(AntlrCommonType.class);
    }

    private <E extends AntlrSchemaStatement> E getTypedStatement(AntlrTypeReference reference, Class<E> target) {
        AntlrSchemaStatement stmt = statements.get(reference);
        if (target.isInstance(stmt)) {
            return target.cast(stmt);
        }
        return null;
    }

    public AntlrSchemaStatement getStatement(AntlrTypeReference ref) {
        return statements.get(ref);
    }

    public AntlrEntityType getEntityType(AntlrTypeReference ref) {
        return getTypedStatement(ref, AntlrEntityType.class);
    }

    public AntlrRecordEntityType getRecordEntityType(AntlrTypeReference ref) {
        return getTypedStatement(ref, AntlrRecordEntityType.class);
    }

    public AntlrEnumEntityType getEnumEntityType(AntlrTypeReference ref) {
        return getTypedStatement(ref, AntlrEnumEntityType.class);
    }

    public AntlrCommonType getCommonType(AntlrTypeReference ref) {
        return getTypedStatement(ref, AntlrCommonType.class);
    }

    public AntlrAction getAction(AntlrTypeReference ref) {
        return getTypedStatement(ref, AntlrAction.class);
    }

    /**
     * Check whether a reference corresponds to an actual construct within a schema
     */
    public boolean isValid(AntlrTypeReference reference) {
        return statements.containsKey(reference);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        Map<String, List<AntlrSchemaStatement>> namespaces = new HashMap<>();
        statements.values().forEach(v -> {
            namespaces.computeIfAbsent(v.getNamespace(), k -> new ArrayList<>()).add(v);
        });

        namespaces.forEach((ns, stmts) -> {
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
    public static AntlrSchema build(Collection<AntlrSchemaStatement> statements) {
        // Ensure no illegal shadowing
        AntlrSchema result = uniquenessPass(statements);
        result = shallowResolutionPass(result);
        result = typeDependencyPass(result);
        return result;
    }

    // The first step of schema generation.
    // The resulting schema contains no duplicates (i.e., illegally shadowed statements)
    // This includes similarly named statements within the same namespace and conflicts with
    // global
    private static AntlrSchema uniquenessPass(Collection<AntlrSchemaStatement> statements) {
        // Sort the list of statements such that entities from the global namespace come first
        statements = statements.stream()
                .sorted(Comparator.comparingInt(a -> a.getNamespace().length()))
                .toList();

        Map<AntlrTypeReference, AntlrSchemaStatement> types = new HashMap<>();
        for (AntlrSchemaStatement stmt : statements) {
            AntlrTypeReference ref = stmt.getTypeReference();
            // Try and lookup the reference by name or by basename in case it exists in the global namespace
            AntlrSchemaStatement lookup = types.containsKey(ref) ?
                    types.get(ref) : types.get(new AntlrTypeReference("", ref.getBaseName()));
            if (lookup != null) {
                throw new SchemaResolutionException("Reference %s illegally shadowed"
                        .formatted(lookup.getTypeReference()));
            }
            types.put(ref, stmt);
        }

        return new AntlrSchema(types);
    }

    // The second stage of resolution.
    // - Ensure all references are valid
    // - Resolve references to built-in primitive types (references to common types persist)
    private static AntlrSchema shallowResolutionPass(AntlrSchema schema) {
        Map<AntlrTypeReference, AntlrSchemaStatement> resolved = new HashMap<>();
        schema.statements.forEach((ref, stmt) -> {
            AntlrStatementResolutionVisitor resolver =
                    new AntlrStatementResolutionVisitor(schema, stmt.getNamespace());
            resolved.put(ref, stmt.compute(resolver));
        });
        return new AntlrSchema(resolved);
    }

    // The third stage of resolution:
    // ensure that definition of common types are not recursive
    private static AntlrSchema typeDependencyPass(AntlrSchema schema) {
        MutableGraph<AntlrTypeReference> typeGraphBuilder = GraphBuilder
                .directed()
                .allowsSelfLoops(true)
                .build();

        schema.statements()
                .map(s -> s instanceof AntlrCommonType t ? t : null)
                .filter(Objects::nonNull)
                .forEach(c -> {
                    typeGraphBuilder.addNode(c.getTypeReference());
                    c.getDefinition().accept(new AntlrSchemaVisitorAdapter() {
                        @Override
                        public void visitTypeReference(AntlrTypeReference type) {
                            AntlrSchemaStatement stmt = requireNonNull(schema.get(type));
                            if (stmt instanceof AntlrCommonType ct) {
                                typeGraphBuilder.putEdge(c.getTypeReference(), ct.getTypeReference());
                            }
                        }
                    });
                });

        // First check for self-recursion, as later on after building the transitive closure
        // we cannot distinguish from initial edges or edges built by transitive closure
        for (AntlrTypeReference node : typeGraphBuilder.nodes()) {
            if (typeGraphBuilder.successors(node).contains(node)) {
                throw new SchemaResolutionException("Recursive definition of type: " + node);
            }
        }

        // Build transitive closure for cycle detection
        Graph<AntlrTypeReference> typeGraph =
                Graphs.transitiveClosure(typeGraphBuilder,
                        Graphs.TransitiveClosureSelfLoopStrategy.ADD_SELF_LOOPS_FOR_CYCLES);

        for (AntlrTypeReference src : typeGraph.nodes()) {
            for (AntlrTypeReference dest : typeGraph.nodes()) {
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
}
