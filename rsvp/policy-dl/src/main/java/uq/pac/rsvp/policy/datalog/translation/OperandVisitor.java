/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.policy.expr.*;
import uq.pac.rsvp.policy.ast.policy.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.datalog.ast.*;
import uq.pac.rsvp.support.error.TranslationError;

import java.util.Set;
import java.util.function.Function;

import static uq.pac.rsvp.policy.datalog.translation.TranslationConstants.*;

public class OperandVisitor implements PolicyComputationVisitor<DLTerm> {

    // Get a temporary variable. Here we assume that we are generating policies
    // over input variables 'principal', 'resource' and 'action', any variable names
    // other than that should be fine
    private int varCounter = 0;
    private final static String VAR_PREFIX = "var";
    DLTerm getTmpVar() {
        return DLTerm.var(VAR_PREFIX + varCounter++);
    }

    private final TranslationVisitor translation;

    public OperandVisitor(TranslationVisitor translation) {
        this.translation = translation;
    }

    @Override
    public DLTerm visitActionExpr(ActionExpression expr) {
        return new DLString(expr.getQualifiedName());
    }

    @Override
    public DLTerm visitBinaryExpr(BinaryExpression expr) {
        DLTerm lhs = expr.getLeft().compute(this),
                rhs = expr.getRight().compute(this);
        DLArithmeticTerm.Operator op = switch (expr.getOperator()) {
            case Add -> DLArithmeticTerm.Operator.ADD;
            case Sub -> DLArithmeticTerm.Operator.SUB;
            case Mul -> DLArithmeticTerm.Operator.MUL;
            default -> throw new TranslationError("Unsupported binary operator: " + expr.getOperator());
        };

        Function<DLTerm, DLTerm> normalise = t ->
                switch (t) {
                    case DLNumber n -> n;
                    case DLVar v -> new DLFunctor(DLFunctor.Functor.TO_NUMBER, v);
                    case DLArithmeticTerm a -> a;
                    default -> throw new TranslationError("Unsupported term: " + t + "[" + t.getClass().getSimpleName() + "]");
                };
        return new DLArithmeticTerm(normalise.apply(lhs), normalise.apply(rhs), op);
    }

    @Override
    public DLTerm visitEntityExpr(EntityExpression expr) {
        return new DLString(expr.getQualifiedName());
    }

    @Override
    public DLTerm visitStringExpr(StringExpression expr) {
        return new DLString(expr.getValue());
    }

    @Override
    public DLTerm visitLongExpr(LongExpression expr) {
        return new DLNumber(expr.getValue());
    }

    @Override
    public DLTerm visitPropertyAccessExpr(PropertyAccessExpression expr) {
        DLTerm lhs = expr.getObject().compute(this),
                attr = DLTerm.lit(expr.getProperty()),
                rhs = getTmpVar();
        translation.addExpression(new DLAtom(AttributeRuleDecl, lhs, attr, rhs));
        return rhs;
    }

    @Override
    public DLTerm visitVariableExpr(VariableExpression expr) {
        String var = expr.getReference();
        switch (translation.getContext()) {
            case Policy -> {
                Set<String> supported = Set.of(PrincipalVar.getName(), ResourceVar.getName(), ActionVar.getName());
                if (!supported.contains(var)) {
                    throw new TranslationError("Unsupported variable: " + var);
                }
            }
            case Invariant -> {}
        }
        return DLTerm.var(expr.toString());
    }
}
