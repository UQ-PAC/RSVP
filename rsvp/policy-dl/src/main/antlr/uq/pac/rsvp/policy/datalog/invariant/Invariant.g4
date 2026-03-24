grammar Invariant;

// Keywords
FOR:  'for';
ALL:  'all';
SOME: 'some';
NONE: 'none';
IS:   'is';
IN:   'in';

// Comments
COMMENT: '//' ~[\r\n]* -> skip;

// Double-quoted string (no escapes of '"')
STRING : '"' ( ~["\r\n])* '"' ;

// White space
WS: [ \r\n\t]+ -> skip;

// Identifier
ID: [A-Za-z][A-Za-z0-9_]*;

Literal: 'true' | 'false';
Variable: ID;
Attribute: ID ('.' ID)*;
Path: ID ('::' ID)*;
Entity: Path '::' STRING;

expression :
      Literal                       # literalExpr
    | Variable                      # variableExpr
    | Attribute                     # propertyExpr
    | '!' expression                # negationExpr
    | expression '&&' expression    # conjunctionExpr
    | expression '||' expression    # disjunctionExpr
;

invariant:
    FOR op=(ALL|SOME|NONE) expression ';'
;

program : invariant*;