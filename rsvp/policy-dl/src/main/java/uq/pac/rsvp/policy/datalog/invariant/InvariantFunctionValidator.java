package uq.pac.rsvp.policy.datalog.invariant;

import uq.pac.rsvp.policy.ast.schema.type.BuiltinType;
import uq.pac.rsvp.policy.ast.schema.type.SetType;

import java.util.*;

import static uq.pac.rsvp.policy.datalog.invariant.InvariantTyping.*;
import static uq.pac.rsvp.Assertion.require;

/**
 * Validation of function calls within invariant expressions
 */
public class InvariantFunctionValidator {

    private static final Map<String, FunctionValidator> REGISTRY;
    static {
        REGISTRY = new HashMap<>();
        REGISTRY.put("allow", new PolicyFunctionValidator("allow"));
        REGISTRY.put("deny", new PolicyFunctionValidator("deny"));
        REGISTRY.put("isEmpty", new SetIsEmptyFunctionValidator("isEmpty"));
        REGISTRY.put("contains", new SetContainsFunctionValidator("contains"));
        REGISTRY.put("containsAll", new SetContainsSetFunctionValidator("containsAll"));
        REGISTRY.put("containsAny", new SetContainsSetFunctionValidator("containsAny"));
    }

    public static FunctionValidator getValidator(String validator) {
        return REGISTRY.get(validator);
    }

    public abstract static class FunctionValidator {
        protected final String name;
        protected final List<InvariantTyping.TypeTest> self;
        protected final List<List<InvariantTyping.TypeTest>> arguments;
        protected final BuiltinType returnType;

        FunctionValidator(String name, List<InvariantTyping.TypeTest> self, List<List<InvariantTyping.TypeTest>> arguments, BuiltinType returnType) {
            this.name = name;
            this.self = self == null ? List.of() : self;
            this.arguments = List.copyOf(arguments);
            this.returnType = returnType;
        }

        String getName() {
            return name;
        }

        void post(BuiltinType actualSelf, List<BuiltinType> actualArguments) { }

        BuiltinType validate(BuiltinType actualSelf, List<BuiltinType> actualArguments) {
            if (actualSelf != null && self.isEmpty()) {
                throw new InvariantValidator.Error("Function %s requires no object application", getName());
            } else if (actualSelf == null && !self.isEmpty()) {
                throw new InvariantValidator.Error("Function %s requires object application", getName());
            } else if (actualSelf != null) {
                InvariantTyping.expect(actualSelf, self);
            }

            require(arguments != null);
            if (arguments.size() != actualArguments.size()) {
                throw new InvariantValidator.Error("Function %s expects %d arguments, got %d", getName(), arguments.size(), actualArguments.size());
            }

            for (int i = 0; i < actualArguments.size(); i++) {
                expect(actualArguments.get(i), arguments.get(i));
            }
            post(actualSelf, actualArguments);
            return returnType;
        }

        public BuiltinType getReturnType() {
            return returnType;
        }
    }

    static class SetIsEmptyFunctionValidator extends FunctionValidator {
        SetIsEmptyFunctionValidator(String name) {
            super(name, List.of(TSet), List.of(), BooleanType);
        }
    }

    static class SetContainsFunctionValidator extends FunctionValidator {
        SetContainsFunctionValidator(String name) {
            super(name,
                    List.of(TSet),
                    List.of(List.of(TBoolean, TLong, TString, TEntityOrAction)),
                    BooleanType);
        }

        @Override
        void post(BuiltinType actualSelf, List<BuiltinType> actualArguments) {
            require(actualSelf instanceof SetType);
            require(actualArguments.size() == 1);
            require(arguments.size() == 1);

            BuiltinType element = ((SetType) actualSelf).getElementType();
            InvariantTyping.expectCompatible(element, actualArguments.getFirst(), this.arguments.getFirst());
        }
    }

    static class SetContainsSetFunctionValidator extends FunctionValidator {
        SetContainsSetFunctionValidator(String name) {
            super(name, List.of(TSet), List.of(List.of(TSet)), BooleanType);
        }

        @Override
        void post(BuiltinType actualSelf, List<BuiltinType> actualArguments) {
            require(actualSelf instanceof SetType);
            require(actualArguments.size() == 1);
            require(actualArguments.getFirst() instanceof SetType);
            require(arguments.size() == 1);

            BuiltinType selfElement = ((SetType) actualSelf).getElementType();
            BuiltinType argElement = ((SetType) actualArguments.getFirst()).getElementType();

            InvariantTyping.expectCompatible(selfElement, argElement,
                    getValidator("contains").arguments.getFirst());
        }
    }

    static class PolicyFunctionValidator extends FunctionValidator {
        PolicyFunctionValidator(String name) {
            super(name,
                    List.of(),
                    List.of(List.of(TEntity),
                            List.of(TEntity),
                            List.of(TAction)),
                    BooleanType);
        }
    }
}
