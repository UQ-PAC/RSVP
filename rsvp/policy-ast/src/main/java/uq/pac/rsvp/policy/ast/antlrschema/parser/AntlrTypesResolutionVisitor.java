package uq.pac.rsvp.policy.ast.antlrschema.parser;

import uq.pac.rsvp.policy.ast.antlrschema.AntlrSchema;
import uq.pac.rsvp.policy.ast.antlrschema.statement.*;
import uq.pac.rsvp.policy.ast.antlrschema.type.*;
import uq.pac.rsvp.policy.ast.antlrschema.visitor.AntlrSchemaValueVisitor;
import uq.pac.rsvp.policy.ast.schema.SchemaResolutionException;
import uq.pac.rsvp.support.SourceLoc;

import java.lang.module.ResolutionException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resolution for types in the presence of schema
 */
public class AntlrTypesResolutionVisitor implements AntlrSchemaValueVisitor<AntlrBuiltinType> {

    private final AntlrSchema schema;
    private final String namespace;

    AntlrTypesResolutionVisitor(AntlrSchema schema, String namespace) {
        this.schema = schema;
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }

    @Override
    public AntlrBuiltinType visitRecord(AntlrRecordType type) {
        Map<AntlrRecordType.Attribute, AntlrBuiltinType> attributes =
                type.getAttributes().entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                   v -> v.getValue().compute(this)
                ));
        return new AntlrRecordType(attributes, type.getSourceLoc());
    }

    @Override
    public AntlrBuiltinType visitSet(AntlrSetType type) {
        return new AntlrSetType(type.getElementType().compute(this), type.getSourceLoc());
    }

    /**
     * Get a constructor for a built-in or extension type and null otherwise
     */
    private static Function<SourceLoc, AntlrBuiltinType> isPrimitiveType(String typename) {
        return switch (typename) {
            case "Bool" -> AntlrBooleanType::new;
            case "Long" -> AntlrLongType::new;
            case "String" -> AntlrStringType::new;
            case "duration" -> AntlrDurationType::new;
            case "ipaddr" -> AntlrIpAddressType::new;
            case "datetime" -> AntlrDateTimeType::new;
            case "decimal" -> AntlrDecimalType::new;
            default -> null;
        };
    }

    @Override
    public AntlrBuiltinType visitTypeReference(AntlrTypeReference type) {
        // First we need to check whether we are dealing with a built-in or extension type,
        // i.e., a type that cannot be shadowed
        Function<SourceLoc, AntlrBuiltinType> constructor = isPrimitiveType(type.getBaseName());

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
            AntlrSchemaStatement global = schema.get(new AntlrTypeReference("", type.getBaseName()));
            AntlrSchemaStatement local = null;
            if (!namespace.isEmpty()) {
                local = schema.get(new AntlrTypeReference(namespace, type.getBaseName()));
            }

            if (global != null && local != null) {
                throw new ResolutionException("Illegal shadowing of type: " + type.getBaseName());
            } else if (global != null) {
                return global.getReference();
            } else if (local != null) {
                return local.getReference();
            } else {
                throw new SchemaResolutionException("Invalid type reference: " + type.getBaseName());
            }
        } else {
            AntlrSchemaStatement stmt = schema.get(type);
            if (stmt == null) {
                throw new SchemaResolutionException("Invalid type reference: " + type.getName());
            }
            return stmt.getReference();
        }
    }

    @Override
    public AntlrBuiltinType visitBoolean(AntlrBooleanType type) {
        return type;
    }

    @Override
    public AntlrBuiltinType visitLong(AntlrLongType type) {
        return type;
    }

    @Override
    public AntlrBuiltinType visitString(AntlrStringType type) {
        return type;
    }

    @Override
    public AntlrBuiltinType visitIpAddress(AntlrIpAddressType type) {
        return type;
    }

    @Override
    public AntlrBuiltinType visitDecimal(AntlrDecimalType type) {
        return type;
    }

    @Override
    public AntlrBuiltinType visitDateTime(AntlrDateTimeType type) {
        return type;
    }

    @Override
    public AntlrBuiltinType visitDuration(AntlrDurationType type) {
        return type;
    }
}
