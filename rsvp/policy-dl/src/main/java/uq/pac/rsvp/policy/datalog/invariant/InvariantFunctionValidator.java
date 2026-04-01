package uq.pac.rsvp.policy.datalog.invariant;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;

import java.util.*;

import static uq.pac.rsvp.policy.datalog.invariant.Typing.expect;
import static uq.pac.rsvp.policy.datalog.util.Assertion.require;

public class InvariantFunctionValidator {

    private static final Map<String, FunctionValidator> REGISTRY;
    static {
        REGISTRY = new HashMap<>();
        REGISTRY.put("allow", new AllowFunctionValidator());
        REGISTRY.put("deny", new DenyFunctionValidator());
    }

    static FunctionValidator getValidator(String validator) {
        return REGISTRY.get(validator);
    }

    abstract static class FunctionValidator {
        private final String name;
        private final List<Typing.TypeTest> self;
        private final List<List<Typing.TypeTest>> arguments;

        FunctionValidator(String name, List<Typing.TypeTest> self, List<List<Typing.TypeTest>> arguments) {
            this.name = name;
            this.self = self == null ? List.of() : self;
            this.arguments = arguments;
        }

        FunctionValidator(String name, List<List<Typing.TypeTest>> arguments) {
            this(name, List.of(), arguments);
        }


        String getName() {
            return name;
        }

        void validate(CommonTypeDefinition actualSelf, List<CommonTypeDefinition> actualArguments) {
            if (actualSelf != null && self.isEmpty()) {
                throw new InvariantValidator.Error("Function %s requires object application, found none", getName());
            } else if (actualSelf == null && !self.isEmpty()) {
                throw new InvariantValidator.Error("Function %s requires no object application", getName());
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
        }
    }

    static abstract class PolicyFunctionValidator extends FunctionValidator {
        PolicyFunctionValidator(String name) {
            super(name, List.of(
                    List.of(Typing.TEntity),
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
