package uq.pac.rsvp.policy.ast.schema.parser;

import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.statement.*;
import uq.pac.rsvp.policy.ast.schema.type.*;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.schema.SchemaResolutionException;
import uq.pac.rsvp.support.SourceLoc;

import java.lang.module.ResolutionException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resolution for types in the presence of schema
 */
public class TypesResolutionVisitor implements SchemaComputationVisitor<BuiltinType> {

    private final Schema schema;
    private final String namespace;

    TypesResolutionVisitor(Schema schema, String namespace) {
        this.schema = schema;
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }

    @Override
    public BuiltinType visitRecord(RecordType type) {
        Map<RecordType.Attribute, BuiltinType> attributes =
                type.getAttributes().entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                   v -> v.getValue().compute(this)
                ));
        return new RecordType(attributes, type.getSourceLoc());
    }

    @Override
    public BuiltinType visitSet(SetType type) {
        return new SetType(type.getElementType().compute(this), type.getSourceLoc());
    }

    /**
     * Get a constructor for a built-in or extension type and null otherwise
     */
    private static Function<SourceLoc, BuiltinType> isPrimitiveType(String typename) {
        return switch (typename) {
            case "Bool" -> BooleanType::new;
            case "Long" -> LongType::new;
            case "String" -> StringType::new;
            case "duration" -> DurationType::new;
            case "ipaddr" -> IpAddressType::new;
            case "datetime" -> DateTimeType::new;
            case "decimal" -> DecimalType::new;
            default -> null;
        };
    }

    @Override
    public BuiltinType visitTypeReference(TypeReference type) {
        // First we need to check whether we are dealing with a built-in or extension type,
        // i.e., a type that cannot be shadowed
        Function<SourceLoc, BuiltinType> constructor = isPrimitiveType(type.getBaseName());

        // The name is recognised as a primitive type
        if (constructor != null) {
            // Built-in types are either from cedar namespace or are not set at all
            if (type.getNamespace() == null || type.getNamespace().equals("__cedar")) {
                return constructor.apply(type.getSourceLoc());
            } else {
                throw new ResolutionException("Namespaced reference to a builtin type");
            }
        // If the namespace of not recognised as a primitive or an extension type, then
        } else if (type.getNamespace() == null) {
            // If the namespace is unset, the type belongs either to local or global namespace.
            // But ti cannot belong to both
            SchemaStatement global = schema.get(new TypeReference("", type.getBaseName()));
            SchemaStatement local = null;
            if (!namespace.isEmpty()) {
                local = schema.get(new TypeReference(namespace, type.getBaseName(), type.getSourceLoc()));
            }

            if (global != null && local != null) {
                throw new ResolutionException("Illegal shadowing of type: " + type.getBaseName());
            } else if (global != null) {
                return global.getTypeReference().with(type.getSourceLoc());
            } else if (local != null) {
                return local.getTypeReference().with(type.getSourceLoc());
            } else {
                throw new SchemaResolutionException("Invalid type reference: " + type.getBaseName());
            }
        } else {
            SchemaStatement stmt = schema.get(type);
            if (stmt == null) {
                throw new SchemaResolutionException("Invalid type reference: " + type.getName());
            }
            return stmt.getTypeReference().with(type.getSourceLoc());
        }
    }

    @Override
    public BuiltinType visitBoolean(BooleanType type) {
        return type;
    }

    @Override
    public BuiltinType visitLong(LongType type) {
        return type;
    }

    @Override
    public BuiltinType visitString(StringType type) {
        return type;
    }

    @Override
    public BuiltinType visitIpAddress(IpAddressType type) {
        return type;
    }

    @Override
    public BuiltinType visitDecimal(DecimalType type) {
        return type;
    }

    @Override
    public BuiltinType visitDateTime(DateTimeType type) {
        return type;
    }

    @Override
    public BuiltinType visitDuration(DurationType type) {
        return type;
    }
}
