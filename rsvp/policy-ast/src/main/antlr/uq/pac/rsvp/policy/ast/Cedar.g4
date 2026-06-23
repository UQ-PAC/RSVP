/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

grammar Cedar;

// Reserved Keywords
FOR: 'for';
ALL: 'all';
SOME: 'some';
NONE: 'none';
IS: 'is';
IN: 'in';
HAS: 'has';
LIKE: 'like';
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

// function names
functionName: type;

// Literal entity, such as App::Account::"Alice"
entity: type '::' STRING;

attributeName:
    ID | STRING;
attribute:
    attributeName ':' expression;
attributes:
    attribute (',' attribute)* ','?;

expression :
      literal                                                          # literalExpr
    | variable                                                         # variableExpr
    | property                                                         # propertyExpr
    | (property '.')? functionName '(' expressionList? ')'             # callExpr
    | type                                                             # typeExpr
    | entity                                                           # entityExpr
    | STRING                                                           # stringExpr
    | LONG                                                             # longExpr
    | expression IN expression                                         # inExpr
    | expression IS type                                               # isExpr
    | expression HAS attributeName                                     # hasExpr
    | '(' expression ')'                                               # groupingExpr
    | '[' expressionList? ']'                                          # setExpr
    | '{' attributes? '}'                                              # recordExpr
    | '!' expression                                                   # negationExpr
    | '-' expression                                                   # arithNegationExpr
    | expression op='*' expression                                     # arithExpr
    | expression op=('-' | '+') expression                             # arithExpr
    | expression op=(EQ | NEQ | GT | LT | GTE | LTE | LIKE) expression # comparisonExpr
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
condition: (WHEN | UNLESS) '{' expression '}';

// variable scope for both resources and principals
variableScope:
    EQ entity
    | IN entity
    | IS type
    | IS type IN entity;

principal:
    PRINCIPAL variableScope?
;

resource:
    RESOURCE variableScope?
;

action:
    ACTION
    (EQ entity
    | IN entity
    | IN '[' entity (',' entity)* ']')?
;

policy:
    annotation*
    perm=(PERMIT | FORBID)
    '(' principal ',' action ',' resource ')'
    condition*
    ';'
;

program : (invariant | policy)* EOF;
