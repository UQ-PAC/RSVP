package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.expr.EntityExpression;
import uq.pac.rsvp.policy.ast.expr.Expression;
import uq.pac.rsvp.policy.ast.expr.PropertyAccessExpression;
import uq.pac.rsvp.policy.ast.expr.VariableExpression;
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
        return new DLString(expr.getUnquotedName());
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

        String lhsVarType = types.stream().findFirst().get();
        // Translation type for the LHS variable, so we know which relation to use
        TranslationType tt = schema.getTranslationType(lhsVarType);
        error(tt != null,
                "No definition for type: " + lhsVarType);
        TranslationAttribute attr = tt.getAttribute(property);
        error(attr != null,
                "No attribute " + property + " for type: " + lhsVarType);

        // Generated relation
        String relationName = attr.getRelationDecl().getName();
        DLAtom generated = new DLAtom(relationName, lhsVar, rhsVar);
        this.expressions.add(generated);
        this.types = Set.of(TranslationTyping.getTypeName(attr.getType()));
        return rhsVar;
    }

    @Override
    public DLTerm visitVariableExpr(VariableExpression expr) {
        error(expr.getReference() == Context,
                "Context variable is unsupported");
        this.types = typing.get(expr.getReference());
        return DLTerm.var(expr.toString());
    }
}
