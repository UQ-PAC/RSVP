grammar Invariant;

// Keywords
FOR:  'for';
ALL:  'all';
SOME: 'some';
NONE: 'none';
IS:   'is';
IN:   'in';

TRUE:  'true';
FALSE: 'false';

// Comments
COMMENT: '//' ~[\r\n]* -> skip;

// Double-quoted string (no escapes of '"')
STRING : '"' ( ~["\r\n])* '"' ;

// White space
WS: [ \r\n\t]+ -> skip;

// Identifier
ID: [A-Za-z][A-Za-z0-9_]*;

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
    | expression 'in' entity                                           # inExpr
    | expression 'is' type                                             # isExpr
    | expression 'has' ID                                              # hasExpr
    | '(' expression ')'                                               # groupingExpr
    | '!' expression                                                   # negationExpr
    | expression op=('==' | '!=' | '>' | '<' | '>=' | '<=') expression # comparisonExpr
    | expression '&&' expression                                       # conjunctionExpr
    | expression '||' expression                                       # disjunctionExpr
;

invariant:
    FOR op=(ALL|SOME|NONE) expression ';'
;

program : invariant*;