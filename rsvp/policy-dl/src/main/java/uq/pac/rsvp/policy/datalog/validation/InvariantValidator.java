package uq.pac.rsvp.policy.datalog.validation;

import uq.pac.rsvp.policy.ast.policy.expr.*;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.statement.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.statement.EntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.statement.EnumEntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.type.*;
import uq.pac.rsvp.policy.ast.policy.Invariant;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.support.error.TranslationError;
import uq.pac.rsvp.support.error.ValidationError;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uq.pac.rsvp.Assertion.require;

public class InvariantValidator implements PolicyComputationVisitor<BuiltinType> {
    // Overall custom-types including actions types and entity types
    private final Set<TypeReference> types;
    // Types of quantifier variables
    private final Map<String, TypeReference> variables;
    // Types of entities
    private final Schema schema;

    final static BooleanType BooleanType = new BooleanType();
    final static StringType StringType = new StringType();
    final static LongType LongType = new LongType();

    public record TypeTest(Function<BuiltinType, Boolean> test, String expected) { }

    final static TypeTest TBoolean = new TypeTest(t -> t.equals(BooleanType), "__cedar::Bool");
    final static TypeTest TLong = new TypeTest(t -> t.equals(LongType), "__cedar::Long");
    final static TypeTest TString = new TypeTest(t -> t.equals(StringType), "__cedar::String");
    final static TypeTest TSet = new TypeTest(t -> t instanceof SetType, "Set<?>");
    final static TypeTest TRecord = new TypeTest(t -> t instanceof RecordType, "Record");
    final static TypeTest TEntity = new TypeTest(InvariantValidator::isEntity, "Entity");
    final static TypeTest TAction = new TypeTest(InvariantValidator::isAction, "Action");

    private static boolean isAction(BuiltinType type) {
        return type instanceof TypeReference ref && ref.getBaseName().equals("Action");
    }

    private static boolean isEntity(BuiltinType type) {
        return type instanceof TypeReference ref && !ref.getBaseName().equals("Action");
    }

    static void expect(BuiltinType actual, TypeTest ...tests) {
        for (TypeTest test : tests) {
            if (test.test().apply(actual)) {
                return;
            }
        }
        String expected = Stream.of(tests).map(TypeTest::expected).toList().toString();
        throw new ValidationError("Expected one of %s, got %s".formatted(expected, actual.toString()));
    }


    static void expect(BuiltinType actual, List<TypeTest> tests) {
        expect(actual, tests.toArray(new TypeTest[0]));
    }

    static void expectCompatible(BuiltinType one, BuiltinType another) {
        if (!one.equals(another)) {
            throw new ValidationError("Incompatible types: %s <> %s".formatted(one, another));
        }
    }

    private InvariantValidator(InvariantValidator factory, Invariant invariant) {
        this.types = Set.copyOf(factory.types);
        this.variables = getVariables(invariant, types);
        this.schema = factory.schema;
    }

    /**
     * Public constructor. Acts like a factory in that it builds internal structures,
     * such as available types, entities and actions. It deliberately leaves invariant-specific
     * structures (i.e., variables) nullified. Invariant validation is done via the private
     * constructor that copies general data from this factory object, computes invariant-specific
     * information and does validation.
     */
    public InvariantValidator(Schema schema) {
        this.types = new HashSet<>();
        this.variables = null;
        this.schema = schema.resolveCommonTypes();
        // Common types should have been eliminated by this point
        require(schema.types().findAny().isEmpty());

        // Build types from entities
        schema.entityTypes().forEach(e -> types.add(e.getTypeReference()));
        // Build types from actions.
        // Actions in Cedar are basically specific entities, for the purpose of type checking we
        // make actions be equivalent, i.e., rather than reason about specific actions an action type
        // is an entity type named `Action` in a particular namespace
        schema.actions().forEach(e -> types.add(new TypeReference(e.getNamespace(), "Action")));
    }

    private static Map<String, TypeReference> getVariables(Invariant invariant, Set<TypeReference> types) {
        Map<String, TypeReference> variables = new HashMap<>();
        invariant.getQuantifier().getVariables().forEach(var -> {
            // Variable type
            TypeReference typeRef = TypeReference.parse(var.type().getValue());
            String varName = var.name().getReference();

            // Check for duplicate variables
            if (variables.containsKey(varName)) {
                throw new ValidationError(
                        "duplicate variable name: %s in quantifier: %s".formatted(varName, invariant.getQuantifier()));
            }

            // Ensure types exist
            if (!types.contains(typeRef)) {
                throw new ValidationError("invalid type: %s in quantifier: %s. Available types: %s".formatted(
                        var.type(), invariant.getQuantifier(), types));
            }

            variables.put(varName, typeRef);
        });
        return variables;
    }

    private BuiltinType collect(Expression expr) {
        return Objects.requireNonNull(expr.compute(this));
    }

    private List<BuiltinType> collect(Collection<Expression> exprs) {
        return exprs.stream().map(this::collect).collect(Collectors.toCollection(ArrayList::new));
    }

    public void validate(Invariant invariant) {
        require(this.variables == null);
        InvariantValidator validator = new InvariantValidator(this, invariant);
        expect(validator.collect(invariant.getExpression()), TBoolean);
    }

    @Override
    public BuiltinType visitBinaryExpr(BinaryExpression expr) {
        BuiltinType lhs = collect(expr.getLeft());
        BuiltinType rhs = collect(expr.getRight());

        return switch (expr.getOp()) {
            case Add, Sub, Mul -> {
                expect(lhs, TLong);
                expect(rhs, TLong);
                yield LongType;
            }
            case Less, LessEq, Greater, GreaterEq -> {
                expect(lhs, TLong);
                expect(rhs, TLong);
                yield BooleanType;
            }
            case Eq, Neq -> {
                expectCompatible(lhs, rhs);
                yield BooleanType;
            }
            case Or, And -> {
                expect(lhs, TBoolean);
                expect(rhs, TBoolean);
                yield BooleanType;
            }
            case In -> {
                expectCompatible(lhs, rhs);
                expect(lhs, TEntity, TAction);
                yield BooleanType;
            }
            default -> throw new TranslationError("Unsupported");
        };
    }

    @Override
    public BuiltinType visitIsExpr(IsExpression expr) {
        expect(collect(expr.getExpression()), TEntity, TAction);
        TypeReference ref = TypeReference.parse(expr.getTypeExpression().getValue());
        if (!types.contains(ref)) {
            throw new ValidationError("invalid type: %s in type expression: %s. Available types: %s"
                    .formatted(ref, expr, types));
        }
        return BooleanType;
    }

    @Override
    public BuiltinType visitHasExpr(HasExpression expr) {
        expect(collect(expr.getExpression()), TEntity, TAction, TRecord);
        return BooleanType;
    }

    @Override
    public BuiltinType visitPropertyAccessExpr(PropertyAccessExpression expr) {
        BuiltinType objectType = collect(expr.getObject());
        if (objectType instanceof TypeReference ref) {
            objectType = switch (schema.get(ref)) {
                case EntityTypeDefinition t -> t.getShape();
                case CommonTypeDefinition t -> t.getDefinition();
                default -> null;
            };
        }
        if (objectType instanceof RecordType rec) {
            BuiltinType attrType = rec.getAttribute(expr.getProperty());
            if (attrType != null) {
                return attrType;
            }
        }
        throw new ValidationError("Invalid property access: %s [%s: %s]"
                .formatted(expr, expr.getObject(), objectType.toString()));
    }

    @Override
    public BuiltinType visitUnaryExpr(UnaryExpression expr) {
        BuiltinType type = collect(expr.getExpression());
        return switch (expr.getOp()) {
            case Not -> {
                expect(type, TBoolean);
                yield BooleanType;
            }
            case Neg -> {
                expect(type, TLong);
                yield LongType;
            }
        };
    }

    @Override
    public BuiltinType visitBooleanExpr(BooleanExpression expr) {
        return BooleanType;
    }

    @Override
    public BuiltinType visitVariableExpr(VariableExpression expr) {
        String ref = expr.getReference();
        if (variables.containsKey(ref)) {
            return variables.get(ref);
        }
        throw new ValidationError("Ungrounded variable: " + ref);
    }

    @Override
    public BuiltinType visitLongExpr(LongExpression expr) {
        return LongType;
    }

    @Override
    public BuiltinType visitStringExpr(StringExpression expr) {
        return StringType;
    }

    @Override
    public BuiltinType visitEntityExpr(EntityExpression expr) {
        TypeReference ref = TypeReference.parse(expr.getType());

        EnumEntityTypeDefinition enumType = schema.getEnumEntityType(ref);
        // We do not check specific entities, but since enum
        // entities are specified by the schema we check if they exist
        if (enumType != null && !enumType.getEnumNames().contains(expr.getName())) {
            throw new ValidationError("invalid enum entity reference: " + expr.getQualifiedName());
        }

        if (types.contains(ref)) {
            return ref;
        }
        throw new ValidationError("invalid type reference: " + expr.getQualifiedName());
    }

    @Override
    public BuiltinType visitActionExpr(ActionExpression expr) {
        TypeReference action = TypeReference.parse(expr.getQualifiedName());
        if (schema.getAction(action) == null) {
            throw new ValidationError("invalid action: " + action);
        }
        TypeReference ref = TypeReference.parse(expr.getType());
        if (types.contains(ref)) {
            return ref;
        }
        throw new ValidationError("invalid type reference: " + ref);
    }

    @Override
    public BuiltinType visitCallExpr(CallExpression expr) {
        String name = expr.getFunc();
        InvariantFunctionValidator.FunctionValidator validator =
                InvariantFunctionValidator.getValidator(name);
        if (validator == null) {
            throw new ValidationError("Function: " + name + " not registered");
        }
        BuiltinType self = expr.getSelf() == null ? null : collect(expr.getSelf());
        return validator.validate(self, collect(expr.getArgs()));
    }

    @Override
    public BuiltinType visitConditionalExpr(ConditionalExpression expr) {
        BuiltinType condition = collect(expr.getCondition());
        expect(condition, TBoolean);
        BuiltinType then = collect(expr.getThen()),
                els = collect(expr.getElse());
        expectCompatible(then, els);
        return then;
    }

    @Override
    public BuiltinType visitSetExpr(SetExpression expr) {
        // We need to make sure sets are homogenous
        List<BuiltinType> types = collect(expr.getElements());
        // Empty set literals are forbidden in Cedar
        if (types.isEmpty()) {
            throw new ValidationError("Empty set literals are forbidden im policies");
        }
        BuiltinType type = types.removeLast();
        types.forEach(t -> expectCompatible(type, t));
        return new SetType(type);
    }

    @Override
    public BuiltinType visitRecordExpr(RecordExpression expr) {
        Map<RecordType.Attribute, BuiltinType> attributes =
                expr.getProperties().entrySet().stream().collect(Collectors.toMap(
                        e -> new RecordType.Attribute(e.getKey()),
                        e -> collect(e.getValue())));
        return new RecordType(attributes);
    }
}
