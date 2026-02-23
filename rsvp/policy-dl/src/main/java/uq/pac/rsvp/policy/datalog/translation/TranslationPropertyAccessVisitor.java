package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.expr.Expression;
import uq.pac.rsvp.policy.ast.expr.PropertyAccessExpression;
import uq.pac.rsvp.policy.ast.expr.VariableExpression;
import uq.pac.rsvp.policy.ast.schema.attribute.AttributeType;
import uq.pac.rsvp.policy.ast.schema.attribute.EntityOrCommonType;
import uq.pac.rsvp.policy.ast.schema.attribute.PrimitiveType;
import uq.pac.rsvp.policy.datalog.ast.DLAtom;
import uq.pac.rsvp.policy.datalog.ast.DLTerm;

import static uq.pac.rsvp.policy.datalog.util.Assertion.require;

// Code generation for property access expressions
public class TranslationPropertyAccessVisitor extends TranslationValueAdapter<String> {
    private final TypeInfo types;
    private AttributeType type;

    public TranslationPropertyAccessVisitor(TranslationSchema schema, TypeInfo types) {
        super(schema);
        this.types = types;
        this.type = null;
    }

    @Override
    public String visitPropertyAccessExpr(PropertyAccessExpression expr) {
        Expression object = expr.getObject();
        String property = expr.getProperty();

        // LHS variable name
        String lhsVar = object.compute(this);
        // Type of the LHS variable (or sub-expression)
        EntityOrCommonType lhsVarType = (EntityOrCommonType)
                (this.type == null ? types.get(lhsVar) : this.type);
        // Translation type for the LHS variable, so we know which relation to use
        TranslationType tt = schema.getTranslationType(lhsVarType.getName());
        TranslationAttribute attr = tt.getAttribute(property);
        // RHS variable
        String rhsVar = TranslationNameGenerator.getVar();
        // Generated relation
        String relationName = attr.getRelationDecl().getName();
        DLAtom generated = new DLAtom(relationName, DLTerm.var(lhsVar), DLTerm.var(rhsVar));
        this.expressions.add(generated);
        this.type = attr.getType();
        // TODO: Support for more types
        require(type instanceof EntityOrCommonType || type instanceof PrimitiveType);
        return rhsVar;
    }

    @Override
    public String visitVariableExpr(VariableExpression expr) {
        // FIXME: Fix references be lowercase
        return expr.toString();
    }
}
