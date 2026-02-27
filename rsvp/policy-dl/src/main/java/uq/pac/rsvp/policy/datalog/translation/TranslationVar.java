package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.ast.expr.VariableExpression;
import uq.pac.rsvp.policy.datalog.ast.*;

import java.util.List;

public class TranslationVar {
    public final static DLVar PrincipalVar =
            new DLVar(VariableExpression.Reference.Principal.getValue());
    public final static DLVar ResourceVar =
            new DLVar(VariableExpression.Reference.Resource.getValue());
    public final static DLVar ActionVar =
            new DLVar(VariableExpression.Reference.Action.getValue());

    public final static DLDeclTerm PrincipalVarDecl =
            DLDeclTerm.symbolic(PrincipalVar.getName());
    public final static DLDeclTerm ResourceVarDecl =
            DLDeclTerm.symbolic(ResourceVar.getName());
    public final static DLDeclTerm ActionVarDecl =
            DLDeclTerm.symbolic(ActionVar.getName());

    public static List<DLTerm> VarList =
            List.of(PrincipalVar, ResourceVar, ActionVar);

    public static List<DLDeclTerm> VarDeclList =
            List.of(PrincipalVarDecl, ResourceVarDecl, ActionVarDecl);

    public static DLAtom makePolicyAtom(String name) {
        return new DLAtom(name, VarList);
    }

    public static DLRuleDecl makePolicyRuleDecl(String name) {
        return new DLRuleDecl(name, VarDeclList);
    }
}
