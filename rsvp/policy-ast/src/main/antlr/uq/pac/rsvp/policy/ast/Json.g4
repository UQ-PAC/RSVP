grammar Json;

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
