grammar Invariant;

// Reserved Keywords
FOR:   'for';
ALL:   'all';
SOME:  'some';
NONE:  'none';
IS:    'is';
IN:    'in';
HAS:   'has';
WHERE: 'where';
TRUE:  'true';
FALSE: 'false';

// Identifier
ID: [A-Za-z][A-Za-z0-9_]*;

// Comments
COMMENT: '//' ~[\r\n]* -> skip;

// Double-quoted string (no escapes or '"' as of now)
STRING : '"' ( ~["\r\n])* '"' ;

// Number
LONG: [-]? [0-9]+;

// White space
WS: [ \r\n\t]+ -> skip;

literal: TRUE | FALSE;
variable: ID;
property: ID ('.' ID)*;
type: ID ('::' ID)*;
entity: type '::' STRING;

expression :
      literal                                                          # literalExpr
    | variable                                                         # variableExpr
    | property                                                         # propertyExpr
    | type                                                             # typeExpr
    | entity                                                           # entityExpr
    | STRING                                                           # stringExpr
    | LONG                                                             # longExpr
    | expression IN entity                                             # inExpr
    | expression IS type                                               # isExpr
    | expression HAS ID                                                # hasExpr
    | '(' expression ')'                                               # groupingExpr
    | '!' expression                                                   # negationExpr
    | expression op=('==' | '!=' | '>' | '<' | '>=' | '<=') expression # comparisonExpr
    | expression '&&' expression                                       # conjunctionExpr
    | expression '||' expression                                       # disjunctionExpr
;

typedVariable: variable IS type;
typedVariableList: typedVariable (',' typedVariable)*;

invariant:
    FOR op=(ALL|SOME|NONE) expression (WHERE typedVariableList)? ';'
;

program : invariant*;