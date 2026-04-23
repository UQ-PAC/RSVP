grammar Cedar;

// Reserved Keywords
FOR: 'for';
ALL: 'all';
SOME: 'some';
NONE: 'none';
IS: 'is';
IN: 'in';
HAS: 'has';
WHERE: 'where';
TRUE: 'true';
FALSE: 'false';
WHEN: 'when';
UNLESS: 'unless';
PERMIT: 'permit';
FORBID: 'forbid';
INVARIANT: 'invariant';
IF: 'if';
THEN: 'then';
ELSE: 'else';
PRINCIPAL: 'principal';
RESOURCE: 'resource';
ACTION: 'action';

// Operators
EQ: '==';
NEQ: '!=';
LTE: '<=';
LT: '<';
GT: '>';
GTE: '>=';

// Brackets
LBRACKET: '[';
RBRACKET: ']';
LPAREN: '(';
RPAREN: ')';
LBRACE: '{';
RBRACE: '}';

// Identifier
ID: [A-Za-z][A-Za-z0-9_]*;

// Comments
COMMENT: '//' ~[\r\n]* -> skip;

fragment ESCAPE: '\\' [tbnr"\\];

// Double-quoted string with standard escapes
STRING : '"' ( ~["\r\n] | ESCAPE )* '"' ;

// Number
LONG: [-]? [0-9]+;

// White space
WS: [ \r\n\t]+ -> skip;

literal: TRUE | FALSE;
variable: PRINCIPAL | ACTION | RESOURCE | ID;
property: variable ('.' variable )*;

// Entity or action type, such as App::Account
type: ID ('::' ID)*;

// Literal entity, such as App::Account::"Alice"
entity: type '::' STRING;

expression :
      literal                                                          # literalExpr
    | variable                                                         # variableExpr
    | property                                                         # propertyExpr
    | (property '.')? ID '(' expressionList? ')'                       # callExpr
    | type                                                             # typeExpr
    | entity                                                           # entityExpr
    | STRING                                                           # stringExpr
    | LONG                                                             # longExpr
    | expression IN expression                                         # inExpr
    | expression IS type                                               # isExpr
    | expression HAS attr=(ID | STRING)                                # hasExpr
    | '(' expression ')'                                               # groupingExpr
    | '[' expressionList? ']'                                          # setExpr
    | '!' expression                                                   # negationExpr
    | '-' expression                                                   # arithNegationExpr
    | expression op='*' expression                                     # arithExpr
    | expression op=('-' | '+') expression                             # arithExpr
    | expression op=(EQ | NEQ | GT | LT | GTE | LTE) expression        # comparisonExpr
    | expression '&&' expression                                       # conjunctionExpr
    | expression '||' expression                                       # disjunctionExpr
    | IF expression THEN expression ELSE expression                    # conditionalExpr
;

expressionList:
    expression (',' expression)*;

typedVariable: variable ':' type;
quantifier:
    FOR quant=(ALL|SOME|NONE) typedVariable (',' typedVariable)*
;

invariant:
    INVARIANT expression quantifier? ';'
;

annotation: '@' ID | '@' ID '(' STRING ')';
when: WHEN '{' expression '}';
unless: UNLESS '{' expression '}';

principal:
    PRINCIPAL
    | PRINCIPAL EQ entity
    | PRINCIPAL IN entity
    | PRINCIPAL IS type
    | PRINCIPAL IS type IN entity
;

resource:
    RESOURCE
    | RESOURCE EQ entity
    | RESOURCE IN entity
    | RESOURCE IS type
    | RESOURCE IS type IN entity
;

action:
    ACTION
    | ACTION EQ entity
    | ACTION IN entity
    | ACTION IN '[' entity (',' entity)* ']'
;

policy:
    annotation*
    perm=(PERMIT | FORBID)
       '(' principal ',' action ',' resource ')'
        when?
        unless?
    ';'
;

program : (invariant | policy)*;