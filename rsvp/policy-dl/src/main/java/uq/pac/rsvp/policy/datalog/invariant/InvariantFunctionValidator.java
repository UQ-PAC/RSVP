package uq.pac.rsvp.policy.datalog.invariant;

import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrBuiltinType;
import uq.pac.rsvp.policy.ast.antlrschema.type.AntlrSetType;

import java.util.*;
import java.util.function.BiFunction;

import static uq.pac.rsvp.policy.datalog.invariant.InvariantTyping.*;
import static uq.pac.rsvp.Assertion.require;

/**
 * Validation of function calls within invariant expressions
 */
public class InvariantFunctionValidator {

    private static final Map<String, BiFunction<String, InvariantTyping, FunctionValidator>> REGISTRY;
    static {
        REGISTRY = new HashMap<>();
        REGISTRY.put("allow", PolicyFunctionValidator::new);
        REGISTRY.put("deny", PolicyFunctionValidator::new);
        REGISTRY.put("isEmpty", SetIsEmptyFunctionValidator::new);
        REGISTRY.put("contains", SetContainsFunctionValidator::new);
        REGISTRY.put("containsAll", SetContainsSetFunctionValidator::new);
        REGISTRY.put("containsAny", SetContainsSetFunctionValidator::new);
    }

    public static FunctionValidator getValidator(String validator, InvariantTyping typing) {
        return REGISTRY.get(validator).apply(validator, typing);
    }

    public abstract static class FunctionValidator {
        protected final String name;
        protected final InvariantTyping typing;
        protected final List<InvariantTyping.TypeTest> self;
        protected final List<List<InvariantTyping.TypeTest>> arguments;
        protected final AntlrBuiltinType returnType;

        FunctionValidator(String name, InvariantTyping typing, List<InvariantTyping.TypeTest> self, List<List<InvariantTyping.TypeTest>> arguments, AntlrBuiltinType returnType) {
            this.name = name;
            this.self = self == null ? List.of() : self;
            this.arguments = List.copyOf(arguments);
            this.returnType = returnType;
            this.typing = typing;
        }

        String getName() {
            return name;
        }

        void post(AntlrBuiltinType actualSelf, List<AntlrBuiltinType> actualArguments) { }

        AntlrBuiltinType validate(AntlrBuiltinType actualSelf, List<AntlrBuiltinType> actualArguments) {
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

        public AntlrBuiltinType getReturnType() {
            return returnType;
        }
    }

    static class SetIsEmptyFunctionValidator extends FunctionValidator {
        SetIsEmptyFunctionValidator(String name, InvariantTyping typing) {
            super(name, typing, List.of(TSet), List.of(), BooleanType);
        }
    }

    static class SetContainsFunctionValidator extends FunctionValidator {
        SetContainsFunctionValidator(String name, InvariantTyping typing) {
            super(name, typing,
                    List.of(TSet),
                    List.of(List.of(TBoolean, TLong, TString, typing.TEntityOrAction)),
                    BooleanType);
        }

        @Override
        void post(AntlrBuiltinType actualSelf, List<AntlrBuiltinType> actualArguments) {
            require(actualSelf instanceof AntlrSetType);
            require(actualArguments.size() == 1);
            require(arguments.size() == 1);

            AntlrBuiltinType element = ((AntlrSetType) actualSelf).getElementType();
            InvariantTyping.expectCompatible(element, actualArguments.getFirst(), this.arguments.getFirst());
        }
    }

    static class SetContainsSetFunctionValidator extends FunctionValidator {
        SetContainsSetFunctionValidator(String name, InvariantTyping typing) {
            super(name, typing, List.of(TSet), List.of(List.of(TSet)), BooleanType);
        }

        @Override
        void post(AntlrBuiltinType actualSelf, List<AntlrBuiltinType> actualArguments) {
            require(actualSelf instanceof AntlrSetType);
            require(actualArguments.size() == 1);
            require(actualArguments.getFirst() instanceof AntlrSetType);
            require(arguments.size() == 1);

            AntlrBuiltinType selfElement = ((AntlrSetType) actualSelf).getElementType();
            AntlrBuiltinType argElement = ((AntlrSetType) actualArguments.getFirst()).getElementType();

            InvariantTyping.expectCompatible(selfElement, argElement,
                    getValidator("contains", typing).arguments.getFirst());
        }
    }

    static class PolicyFunctionValidator extends FunctionValidator {
        PolicyFunctionValidator(String name, InvariantTyping typing) {
            super(name, typing,
                    List.of(),
                    List.of(List.of(typing.TEntity),
                            List.of(typing.TEntity),
                            List.of(typing.TAction)),
                    BooleanType);
        }
    }
}
