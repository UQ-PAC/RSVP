package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.expr.EntityExpression;
import uq.pac.rsvp.policy.ast.expr.Expression;
import uq.pac.rsvp.policy.ast.expr.PropertyAccessExpression;
import uq.pac.rsvp.policy.ast.expr.VariableExpression;
import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.common.EntityTypeReference;
import uq.pac.rsvp.policy.ast.schema.common.LongType;
import uq.pac.rsvp.policy.ast.schema.common.StringType;
import uq.pac.rsvp.policy.datalog.ast.*;

import static uq.pac.rsvp.policy.datalog.util.Assertion.require;

// Code generation for property access expressions
public class TranslationOperandVisitor extends TranslationValueAdapter<String> {
    private final TypeInfo types;
    private CommonTypeDefinition type;

    public TranslationOperandVisitor(TranslationSchema schema, TypeInfo types) {
        super(schema);
        this.types = types;
        this.type = null;
    }

    CommonTypeDefinition getType() {
        return type;
    }

    @Override
    public String visitEntityExpr(EntityExpression expr) {
        DLVar var = TranslationNameGenerator.getVar();
        DLString val = new DLString(String.join("::", expr.getQualifiedEid()));
        expr.getQualifiedEid();
        DLRuleExpr re = new DLConstraint(var, val, DLConstraint.Operator.EQ);
        expressions.add(re);
        return var.getName();
    }

    @Override
    public String visitPropertyAccessExpr(PropertyAccessExpression expr) {
        Expression object = expr.getObject();
        String property = expr.getProperty();

        // LHS variable name
        String lhsVar = object.compute(this);
        // Type of the LHS variable (or sub-expression)
        EntityTypeReference lhsVarType = (EntityTypeReference)
                (this.type == null ? types.get(lhsVar) : this.type);
        // Translation type for the LHS variable, so we know which relation to use
        TranslationType tt = schema.getTranslationType(lhsVarType.getTypename());
        TranslationAttribute attr = tt.getAttribute(property);
        // RHS variable
        DLVar rhsVar = TranslationNameGenerator.getVar();
        // Generated relation
        String relationName = attr.getRelationDecl().getName();
        DLAtom generated = new DLAtom(relationName, DLTerm.var(lhsVar), rhsVar);
        this.expressions.add(generated);
        this.type = attr.getType();
        // TODO: Support for more types
        require(type instanceof EntityTypeReference ||
                type instanceof StringType ||
                type instanceof LongType);
        return rhsVar.getName();
    }

    @Override
    public String visitVariableExpr(VariableExpression expr) {
        // FIXME: Fix references be lowercase
        return expr.toString();
    }
}
