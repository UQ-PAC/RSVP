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
variable: ID;
property: ID ('.' ID)*;

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
    | expression op=('==' | '!=' | '>' | '<' | '>=' | '<=') expression # comparisonExpr
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
when: WHEN '{'  expression '}';
unless: UNLESS '{'  expression '}';

policy:
    annotation*
    perm=(PERMIT | FORBID) '('
        expression ','
        expression ','
        expression ')'
        when?
        unless?
    ';'?
;

program : (invariant | policy)*;