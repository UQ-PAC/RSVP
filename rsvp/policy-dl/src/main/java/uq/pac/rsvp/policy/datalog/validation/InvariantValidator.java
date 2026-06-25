/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.datalog.validation;

import uq.pac.rsvp.policy.ast.AstNode;
import uq.pac.rsvp.policy.ast.policy.expr.*;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyPayloadVisitor;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.statement.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.statement.EntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.statement.EnumEntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.type.*;
import uq.pac.rsvp.policy.ast.policy.Invariant;
import uq.pac.rsvp.support.error.TranslationError;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uq.pac.rsvp.Assertion.require;
import static uq.pac.rsvp.policy.datalog.validation.InvariantValidator.Payload;

public class InvariantValidator implements PolicyPayloadVisitor<BuiltinType, Payload> {
    // Types including comprising entities and actions
    private final Set<TypeReference> types;
    // Types of entities
    private final Schema schema;

    /**
     * Abstraction for the visitor payload. At all stages of resolution the information
     * that is being passed around is the mapping from variables to their types
     */
    public static class Payload extends HashMap<String, TypeReference> { }

    final static BooleanType BooleanType = new BooleanType();
    final static StringType StringType = new StringType();
    final static LongType LongType = new LongType();

    public record TypeTest(Function<BuiltinType, Boolean> test, String expected) { }

    final static TypeTest TBoolean = new TypeTest(t -> t instanceof BooleanType, "__cedar::Bool");
    final static TypeTest TLong = new TypeTest(t -> t instanceof LongType, "__cedar::Long");
    final static TypeTest TString = new TypeTest(t -> t instanceof StringType, "__cedar::String");
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

    static void expect(AstNode expr, BuiltinType actual, TypeTest ...tests) {
        for (TypeTest test : tests) {
            if (test.test().apply(actual)) {
                return;
            }
        }
        String expected = Stream.of(tests).map(TypeTest::expected).toList().toString();
        throw new TranslationError("Expected one of %s, got %s".formatted(expected, actual.toString()), expr.getSourceLoc());
    }

    static void expect(AstNode expr, BuiltinType actual, List<TypeTest> tests) {
        expect(expr, actual, tests.toArray(new TypeTest[0]));
    }

    static void expectCompatible(AstNode expr, BuiltinType one, BuiltinType another) {
        if (!one.equals(another)) {
            throw new TranslationError("Incompatible types: %s <> %s".formatted(one, another), expr.getSourceLoc());
        }
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
        this.schema = schema.resolveCommonTypes();

        // Common types should have been eliminated by this point
        require(this.schema.types().findAny().isEmpty());

        // Build types from entities
        this.schema.entityTypes().forEach(e -> types.add(e.getTypeReference()));
        // Build types from actions.
        // Actions in Cedar are basically specific entities, for the purpose of type checking we
        // make actions be equivalent, i.e., rather than reason about specific actions an action type
        // is an entity type named `Action` in a particular namespace
        this.schema.actions().forEach(e -> types.add(new TypeReference(e.getNamespace(), "Action")));
    }

    private static Payload getVariables(Invariant invariant, Set<TypeReference> types) {
        Payload variables = new Payload();
        invariant.getQuantifier().getVariables().forEach(var -> {
            // Variable type
            TypeReference typeRef = TypeReference.parse(var.type().getValue());
            String varName = var.name().getReference();

            // Check for duplicate variables
            if (variables.containsKey(varName)) {
                throw new TranslationError(
                        "duplicate variable name: %s in quantifier: %s".formatted(varName, invariant.getQuantifier()), invariant.getSourceLoc());
            }

            // Ensure types exist
            if (!types.contains(typeRef)) {
                throw new TranslationError("invalid type: %s in quantifier: %s. Available types: %s".formatted(
                        var.type(), invariant.getQuantifier(), types), invariant.getSourceLoc());
            }

            variables.put(varName, typeRef);
        });
        return variables;
    }

    private BuiltinType collect(Expression expr, Payload payload) {
        return Objects.requireNonNull(expr.compute(this, payload));
    }

    private List<BuiltinType> collect(Collection<Expression> exprs, Payload payload) {
        return exprs.stream()
                .map(e -> collect(e, payload))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void validate(Invariant invariant) {
        Payload payload = getVariables(invariant, types);
        invariant.compute(this, payload);
    }

    @Override
    public BuiltinType visitInvariant(Invariant invariant, Payload payload) {
        expect(invariant, collect(invariant.getExpression(), payload), TBoolean);
        return BooleanType;
    }

    @Override
    public BuiltinType visitBinaryExpr(BinaryExpression expr, Payload payload) {
        BuiltinType lhs = collect(expr.getLeft(), payload);
        BuiltinType rhs = collect(expr.getRight(), payload);

        return switch (expr.getOperator()) {
            case Add, Sub, Mul -> {
                expect(expr, lhs, TLong);
                expect(expr, rhs, TLong);
                yield LongType;
            }
            case Less, LessEq, Greater, GreaterEq -> {
                expect(expr, lhs, TLong);
                expect(expr, rhs, TLong);
                yield BooleanType;
            }
            case Eq, Neq -> {
                expectCompatible(expr, lhs, rhs);
                yield BooleanType;
            }
            case Or, And -> {
                expect(expr, lhs, TBoolean);
                expect(expr, rhs, TBoolean);
                yield BooleanType;
            }
            case In -> {
                expectCompatible(expr, lhs, rhs);
                expect(expr, lhs, TEntity, TAction);
                yield BooleanType;
            }
            default -> throw new TranslationError("Unsupported", expr.getSourceLoc());
        };
    }

    @Override
    public BuiltinType visitIsExpr(IsExpression expr, Payload payload) {
        expect(expr, collect(expr.getExpression(), payload), TEntity, TAction);
        TypeReference ref = TypeReference.parse(expr.getTypeExpression().getValue());
        if (!types.contains(ref)) {
            throw new TranslationError("invalid type: %s in type expression: %s. Available types: %s"
                    .formatted(ref, expr, types), expr.getSourceLoc());
        }
        return BooleanType;
    }

    @Override
    public BuiltinType visitHasExpr(HasExpression expr, Payload payload) {
        expect(expr, collect(expr.getExpression(), payload), TEntity, TAction, TRecord);
        return BooleanType;
    }

    @Override
    public BuiltinType visitPropertyAccessExpr(PropertyAccessExpression expr, Payload payload) {
        BuiltinType objectType = collect(expr.getObject(), payload);
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
        throw new TranslationError("Invalid property access: %s [%s: %s]"
                .formatted(expr, expr.getObject(), objectType.toString()), expr.getSourceLoc());
    }

    @Override
    public BuiltinType visitUnaryExpr(UnaryExpression expr, Payload payload) {
        BuiltinType type = collect(expr.getExpression(), payload);
        return switch (expr.getOperator()) {
            case Not -> {
                expect(expr, type, TBoolean);
                yield BooleanType;
            }
            case Neg -> {
                expect(expr, type, TLong);
                yield LongType;
            }
        };
    }

    @Override
    public BuiltinType visitBooleanExpr(BooleanExpression expr, Payload payload) {
        return BooleanType;
    }

    @Override
    public BuiltinType visitVariableExpr(VariableExpression expr, Payload payload) {
        String ref = expr.getReference();
        if (payload.containsKey(ref)) {
            return payload.get(ref);
        }
        throw new TranslationError("Ungrounded variable: " + ref, expr.getSourceLoc());
    }

    @Override
    public BuiltinType visitLongExpr(LongExpression expr, Payload payload) {
        return LongType;
    }

    @Override
    public BuiltinType visitStringExpr(StringExpression expr, Payload payload) {
        return StringType;
    }

    @Override
    public BuiltinType visitEntityExpr(EntityExpression expr, Payload payload) {
        TypeReference ref = TypeReference.parse(expr.getType());

        EnumEntityTypeDefinition enumType = schema.getEnumEntityType(ref);
        // We do not check specific entities, but since enum
        // entities are specified by the schema we check if they exist
        if (enumType != null && !enumType.getEnumNames().contains(expr.getName())) {
            throw new TranslationError("invalid enum entity reference: " + expr.getQualifiedName(), expr.getSourceLoc());
        }

        if (types.contains(ref)) {
            return ref;
        }
        throw new TranslationError("invalid type reference: " + expr.getQualifiedName(), expr.getSourceLoc());
    }

    @Override
    public BuiltinType visitActionExpr(ActionExpression expr, Payload payload) {
        TypeReference action = TypeReference.parse(expr.getQualifiedName());
        if (schema.getAction(action) == null) {
            throw new TranslationError("invalid action: " + action, expr.getSourceLoc());
        }
        TypeReference ref = TypeReference.parse(expr.getType());
        if (types.contains(ref)) {
            return ref;
        }
        throw new TranslationError("invalid type reference: " + ref, expr.getSourceLoc());
    }

    @Override
    public BuiltinType visitCallExpr(CallExpression expr, Payload payload) {
        String name = expr.getFunc();
        InvariantFunctionValidator.FunctionValidator validator =
                InvariantFunctionValidator.getValidator(expr);
        if (validator == null) {
            throw new TranslationError("Function: " + name + " not registered", expr.getSourceLoc());
        }
        BuiltinType self = expr.getSelf() == null ? null : collect(expr.getSelf(), payload);
        return validator.validate(self, collect(expr.getArgs(), payload));
    }

    @Override
    public BuiltinType visitConditionalExpr(ConditionalExpression expr, Payload payload) {
        BuiltinType condition = collect(expr.getCondition(), payload);
        expect(expr, condition, TBoolean);
        BuiltinType then = collect(expr.getThen(), payload),
                els = collect(expr.getElse(), payload);
        expectCompatible(expr, then, els);
        return then;
    }

    @Override
    public BuiltinType visitSetExpr(SetExpression expr, Payload payload) {
        // We need to make sure sets are homogenous
        List<BuiltinType> types = collect(expr.getElements(), payload);
        // Empty set literals are forbidden in Cedar
        if (types.isEmpty()) {
            throw new TranslationError("Empty set literals are forbidden im policies", expr.getSourceLoc());
        }
        BuiltinType type = types.removeLast();
        types.forEach(t -> expectCompatible(expr, type, t));
        return new SetType(type);
    }

    @Override
    public BuiltinType visitRecordExpr(RecordExpression expr, Payload payload) {
        Map<RecordType.Attribute, BuiltinType> attributes =
                expr.getProperties().entrySet().stream().collect(Collectors.toMap(
                        e -> new RecordType.Attribute(e.getKey()),
                        e -> collect(e.getValue(), payload)));
        return new RecordType(attributes);
    }
}
