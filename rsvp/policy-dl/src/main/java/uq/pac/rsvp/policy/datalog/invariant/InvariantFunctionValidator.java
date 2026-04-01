package uq.pac.rsvp.policy.datalog.invariant;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.common.SetTypeDefinition;

import java.util.*;

import static uq.pac.rsvp.policy.datalog.invariant.Typing.*;
import static uq.pac.rsvp.policy.datalog.util.Assertion.require;

/**
 * Validation of function calls within invariant expressions
 */
public class InvariantFunctionValidator {

    private static final Map<String, FunctionValidator> REGISTRY;
    static {
        REGISTRY = new HashMap<>();
        REGISTRY.put("allow", new AllowFunctionValidator());
        REGISTRY.put("deny", new DenyFunctionValidator());
        REGISTRY.put("isEmpty", new SetIsEmptyFunctionValidator());
        REGISTRY.put("contains", new SetContainsFunctionValidator());

        // Ensure that names of the registered functions are the same as registration keys
        REGISTRY.forEach((key, value) -> require(key.equals(value.name)));
    }

    static FunctionValidator getValidator(String validator) {
        return REGISTRY.get(validator);
    }

    abstract static class FunctionValidator {
        protected final String name;
        protected final List<Typing.TypeTest> self;
        protected final List<List<Typing.TypeTest>> arguments;

        FunctionValidator(String name, List<Typing.TypeTest> self, List<List<Typing.TypeTest>> arguments) {
            this.name = name;
            this.self = self == null ? List.of() : self;
            this.arguments = List.copyOf(arguments);
        }

        FunctionValidator(String name, List<List<Typing.TypeTest>> arguments) {
            this(name, List.of(), arguments);
        }

        String getName() {
            return name;
        }

        void post(CommonTypeDefinition actualSelf, List<CommonTypeDefinition> actualArguments) { }

        void validate(CommonTypeDefinition actualSelf, List<CommonTypeDefinition> actualArguments) {
            if (actualSelf != null && self.isEmpty()) {
                throw new InvariantValidator.Error("Function %s requires no object application", getName());
            } else if (actualSelf == null && !self.isEmpty()) {
                throw new InvariantValidator.Error("Function %s requires object application", getName());
            } else if (actualSelf != null) {
                Typing.expect(actualSelf, self);
            }

            require(arguments != null);
            if (arguments.size() != actualArguments.size()) {
                throw new InvariantValidator.Error("Function %s expects %d arguments, got %d", getName(), arguments.size(), actualArguments.size());
            }

            for (int i = 0; i < actualArguments.size(); i++) {
                expect(actualArguments.get(i), arguments.get(i));
            }
            post(actualSelf, actualArguments);
        }
    }

    static class SetIsEmptyFunctionValidator extends FunctionValidator {
        SetIsEmptyFunctionValidator() {
            super("isEmpty", List.of(TSet), List.of());
        }
    }

    static class SetContainsFunctionValidator extends FunctionValidator {
        SetContainsFunctionValidator() {
            super("contains",
                    List.of(TSet),
                    List.of(List.of(TBoolean, TLong, TString, TEntityOrAction)));
        }

        @Override
        void post(CommonTypeDefinition actualSelf, List<CommonTypeDefinition> actualArguments) {
            require(actualSelf instanceof SetTypeDefinition);
            require(actualArguments.size() == 1);
            require(arguments.size() == 1);

            CommonTypeDefinition element = ((SetTypeDefinition) actualSelf).getElementType();
            Typing.expectCompatible(element, actualArguments.getFirst(), this.arguments.getFirst());
        }
    }

    static abstract class PolicyFunctionValidator extends FunctionValidator {
        PolicyFunctionValidator(String name) {
            super(name,
                    List.of(),
                    List.of(List.of(Typing.TEntity),
                            List.of(Typing.TEntity),
                            List.of(Typing.TAction)));
        }
    }

    static class AllowFunctionValidator extends PolicyFunctionValidator {
        AllowFunctionValidator() {
            super("allow");
        }
    }

    static class DenyFunctionValidator extends PolicyFunctionValidator {
        DenyFunctionValidator() {
            super("deny");
        }
    }

}
