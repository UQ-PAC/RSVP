package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.datalog.ast.*;

import java.util.Collections;
import java.util.Set;

import static uq.pac.rsvp.policy.ast.expr.VariableExpression.Reference.Context;
import static uq.pac.rsvp.policy.datalog.translation.TranslationError.error;
import static uq.pac.rsvp.policy.datalog.util.Assertion.require;

// Code generation for property access expressions
public class TranslationOperandVisitor extends TranslationValueAdapter<DLTerm> {
    private Set<String> types;

    public TranslationOperandVisitor(TranslationSchema schema, TranslationTyping typeInfo) {
        super(schema, typeInfo);
        this.types = null;
    }

    @Override
    public DLTerm visitActionExpr(ActionExpression expr) {
        this.types = Collections.singleton(TranslationTyping.getTypeName(expr));
        return new DLString(expr.getQualifiedId());
    }

    @Override
    public DLTerm visitEntityExpr(EntityExpression expr) {
        this.types = Collections.singleton(TranslationTyping.getTypeName(expr));
        return new DLString(expr.getQualifiedEid());
    }

    @Override
    public DLTerm visitStringExpr(StringExpression expr) {
        this.types = Collections.singleton(TranslationTyping.getTypeName(expr));
        return new DLString(expr.getValue());
    }

    @Override
    public DLTerm visitLongExpr(LongExpression expr) {
        this.types = Collections.singleton(TranslationTyping.getTypeName(expr));
        return new DLNumber(expr.getValue());
    }

    @Override
    public DLTerm visitPropertyAccessExpr(PropertyAccessExpression expr) {
        Expression object = expr.getObject();
        String property = expr.getProperty();

        // LHS variable
        DLTerm lhsVar = object.compute(this);
        // RHS variable
        DLVar rhsVar = TranslationNameGenerator.getVar();

        error(!types.isEmpty(), "No applicable types found");
        error(types.size() == 1, "Multiple applicable types found: " + types);
        String lhsVarType = types.stream().findFirst().orElse(null);
        // Translation type for the LHS variable, so we know which relation to use
        TranslationEntityDefinition tt = schema.getTranslationEntityType(lhsVarType);
        error(tt != null, "No definition for type: " + lhsVarType);
        TranslationAttribute attr = tt.getAttribute(property);
        error(attr != null, "No attribute " + property + " for type: " + lhsVarType);

        // Generated relation
        String relationName = attr.getRuleDecl().getName();
        DLAtom generated = new DLAtom(relationName, lhsVar, rhsVar);
        this.expressions.add(generated);
        this.types = Set.of(TranslationTyping.getTypeName(attr.getType()));
        return rhsVar;
    }

    @Override
    public DLTerm visitVariableExpr(VariableExpression expr) {
        error(expr.getReference() != Context, "Unsupported variable: " + expr.getReference());
        this.types = typing.get(expr.getReference());
        return DLTerm.var(expr.toString());
    }

    public String getSingletonType() {
        error(types != null && !types.isEmpty(), "No type information available");
        error(types.size() == 1, "Multiple applicable types: " + types);
        return types.stream().findFirst().orElseThrow();
    }
}
