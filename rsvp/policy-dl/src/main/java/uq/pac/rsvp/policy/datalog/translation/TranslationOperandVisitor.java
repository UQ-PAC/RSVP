package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.datalog.ast.*;

import java.util.Set;

import static uq.pac.rsvp.policy.ast.expr.VariableExpression.Reference.Context;
import static uq.pac.rsvp.policy.datalog.translation.TranslationError.error;

// Code generation for property access expressions
public class TranslationOperandVisitor extends TranslationValueAdapter<DLTerm> {
    private Set<String> types;

    public TranslationOperandVisitor(TranslationSchema schema, TranslationTyping typeInfo) {
        super(schema, typeInfo);
        this.types = null;
    }

    @Override
    public DLTerm visitEntityExpr(EntityExpression expr) {
        return new DLString(String.join("::", expr.getQualifiedEid()));
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
        Expression object = expr.getObject();
        String property = expr.getProperty();

        // LHS variable
        DLTerm lhsVar = object.compute(this);
        // RHS variable
        DLVar rhsVar = TranslationNameGenerator.getVar();

        error(types.size() == 1,
                "No/Multiple applicable types found: " + types);

        String lhsVarType = types.stream().findFirst().orElse(null);
        // Translation type for the LHS variable, so we know which relation to use
        TranslationEntityDefinition tt = schema.getTranslationEntityType(lhsVarType);
        error(tt != null,
                "No definition for type: " + lhsVarType);
        TranslationAttribute attr = tt.getAttribute(property);
        error(attr != null,
                "No attribute " + property + " for type: " + lhsVarType);

        // Generated relation
        String relationName = attr.getRuleDecl().getName();
        DLAtom generated = new DLAtom(relationName, lhsVar, rhsVar);
        this.expressions.add(generated);
        this.types = Set.of(TranslationTyping.getTypeName(attr.getType()));
        return rhsVar;
    }

    @Override
    public DLTerm visitVariableExpr(VariableExpression expr) {
        error(expr.getReference() != Context,
                "Context variable is unsupported");
        this.types = typing.get(expr.getReference());
        return DLTerm.var(expr.toString());
    }
}
