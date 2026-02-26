package uq.pac.rsvp.policy.datalog.ast;

/**
 * STRING ::= ? quoted string ?
 * NUMBER ::= ? integer or floating point number ?
 * IDENT ::= ? alphanumeric identifier ?
 * ====
 * Variable ::= IDENT
 * Term ::= Variable | NUMBER | STRING
 * BinaryOperator ::= '=' | '>' | '<' | '>=' | '<='
 * Constraint ::= Term BinaryOperator Term
 * Atom := IDENT '(' Term [ ',' Term ]* ')'
 * RuleExpression ::= Atom | Constraint
 * Rule := Atom ':-' RuleExpression [',' RuleExpression] '.'
 * Fact := Atom '.'
 * Type := IDENT | 'symbol' | 'number' | 'unsigned' | 'float'
 * DeclTerm := IDENT ':' Type
 * SubTypeDeclaration ::= '.type' '<:' ('symbol' | 'number')
 * UnionTypeDeclaration ::= Type [ '|' Type ]*
 * TypeDeclaration ::= SubTypeDeclaration | UnionTypeDeclaration
 * Directive ::= ('.input' | '.output') IDENT '(' [IDENT = (IDENT | STRING)]* ')'
 * RelationDeclaration ::= '.decl' IDENT '(' DeclTerm [ ',' DeclTerm ]* ')'
 * Constraint ::= Term ( '=' | '>' | '<' | '<=' | '>=' | '!=' ) Term
 * Statement ::= RelationDeclaration | TypeDeclaration | Fact | Rule
 */

public abstract class DLNode {

    private String cache = null;

    protected abstract String stringify();

    @Override
    public final String toString() {
        if (cache == null) {
            cache = stringify();
        }
        return cache;
    }

    @Override
    public boolean equals(Object obj) {
        throw new AssertionError("Expected sub-class implementation of equals not found");
    }
}
