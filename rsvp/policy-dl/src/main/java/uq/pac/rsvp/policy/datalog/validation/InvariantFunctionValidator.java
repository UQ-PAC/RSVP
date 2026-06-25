/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.datalog.validation;

import uq.pac.rsvp.policy.ast.policy.expr.CallExpression;
import uq.pac.rsvp.policy.ast.schema.type.BuiltinType;
import uq.pac.rsvp.policy.ast.schema.type.SetType;
import uq.pac.rsvp.support.error.TranslationError;

import java.util.*;

import static uq.pac.rsvp.Assertion.require;
import static uq.pac.rsvp.policy.datalog.validation.InvariantValidator.*;

/**
 * Validation of function calls within invariant expressions
 */
public class InvariantFunctionValidator {

    public static FunctionValidator getValidator(CallExpression call) {
        return switch (call.getFunc()) {
            case "allow", "deny" -> new PolicyFunctionValidator(call);
            case "isEmpty" -> new SetIsEmptyFunctionValidator(call);
            case "contains" -> new SetContainsFunctionValidator(call);
            case "containsAll", "containsAny" -> new SetContainsSetFunctionValidator(call);
            default -> null;
        };
    }

    public abstract static class FunctionValidator {
        protected final CallExpression call;
        protected final List<TypeTest> self;
        protected final List<List<TypeTest>> arguments;
        protected final BuiltinType returnType;

        FunctionValidator(CallExpression call, List<TypeTest> self, List<List<TypeTest>> arguments, BuiltinType returnType) {
            this.call = call;
            this.self = self == null ? List.of() : self;
            this.arguments = List.copyOf(arguments);
            this.returnType = returnType;
        }

        String getName() {
            return call.getFunc();
        }

        void post(BuiltinType actualSelf, List<BuiltinType> actualArguments) { }

        BuiltinType validate(BuiltinType actualSelf, List<BuiltinType> actualArguments) {
            if (actualSelf != null && self.isEmpty()) {
                throw new TranslationError("Function %s requires no object application".formatted(getName()), call.getSourceLoc());
            } else if (actualSelf == null && !self.isEmpty()) {
                throw new TranslationError("Function %s requires object application".formatted(getName()), call.getSourceLoc());
            } else if (actualSelf != null) {
                InvariantValidator.expect(call, actualSelf, self);
            }

            require(arguments != null);
            if (arguments.size() != actualArguments.size()) {
                throw new TranslationError("Function %s expects %d arguments, got %d"
                        .formatted(getName(), arguments.size(), actualArguments.size()), call.getSourceLoc());
            }

            for (int i = 0; i < actualArguments.size(); i++) {
                InvariantValidator.expect(call, actualArguments.get(i), arguments.get(i));
            }
            post(actualSelf, actualArguments);
            return returnType;
        }

        public BuiltinType getReturnType() {
            return returnType;
        }
    }

    static class SetIsEmptyFunctionValidator extends FunctionValidator {
        SetIsEmptyFunctionValidator(CallExpression call) {
            super(call, List.of(TSet), List.of(), BooleanType);
        }
    }

    static class SetContainsFunctionValidator extends FunctionValidator {
        SetContainsFunctionValidator(CallExpression call) {
            super(call,
                    List.of(TSet),
                    List.of(List.of(TBoolean, TLong, TString, TEntity, TAction)),
                    BooleanType);
        }

        @Override
        void post(BuiltinType actualSelf, List<BuiltinType> actualArguments) {
            require(actualSelf instanceof SetType);
            require(actualArguments.size() == 1);
            require(arguments.size() == 1);

            BuiltinType element = ((SetType) actualSelf).getElementType();
            InvariantValidator.expectCompatible(call,element, actualArguments.getFirst());
            InvariantValidator.expect(call, element, this.arguments.getFirst());
        }
    }

    static class SetContainsSetFunctionValidator extends FunctionValidator {
        SetContainsSetFunctionValidator(CallExpression call) {
            super(call, List.of(TSet), List.of(List.of(TSet)), BooleanType);
        }

        @Override
        void post(BuiltinType actualSelf, List<BuiltinType> actualArguments) {
            require(actualSelf instanceof SetType);
            require(actualArguments.size() == 1);
            require(actualArguments.getFirst() instanceof SetType);
            require(arguments.size() == 1);

            BuiltinType selfElement = ((SetType) actualSelf).getElementType();
            BuiltinType argElement = ((SetType) actualArguments.getFirst()).getElementType();

            InvariantValidator.expectCompatible(call,selfElement, argElement);
            InvariantValidator.expect(call,selfElement, List.of(TBoolean, TLong, TString, TEntity, TAction));
        }
    }

    static class PolicyFunctionValidator extends FunctionValidator {
        PolicyFunctionValidator(CallExpression call) {
            super(call,
                    List.of(),
                    List.of(List.of(TEntity),
                            List.of(TEntity),
                            List.of(TAction)),
                    BooleanType);
        }
    }
}
