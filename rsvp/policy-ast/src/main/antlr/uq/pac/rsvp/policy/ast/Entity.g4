/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

grammar Entity;

fragment ESCAPE: '\\' [tbnr"\\];

// Double-quoted string with standard escapes
STRING : '"' ( ~["\r\n] | ESCAPE )* '"' ;

// Number
NUMBER: [-]? [0-9]+;

value
    : STRING    # stringExpr
    | NUMBER    # numberExpr
    | 'true'    # booleanExpr
    | 'false'   # booleanExpr
    | object    # objectExpr
    | array     # arrayExpr
;

mapping: STRING ':' value;

array
    : '[' value (',' value)* ']'
    | '[' ']'
;

object
    : '{' mapping (',' mapping)* '}'
    | '{' '}'
;

entity: object;

entitySet:
    ('[' ']' | '[' entity (',' entity)* ']') EOF
;

// Skip white space
WS: [ \t\n\r]+ -> skip;

// Skip line comments
LINE_COMMENT: '//' ~[\r\n]* -> skip;
