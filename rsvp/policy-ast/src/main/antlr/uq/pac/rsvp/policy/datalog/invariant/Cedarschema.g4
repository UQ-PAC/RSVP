grammar Cedarschema;

// Identifier
ID: [_A-Za-z][A-Za-z0-9_]*;

// Comments
COMMENT: '//' ~[\r\n]* -> skip;

fragment ESCAPE: '\\' [tbnr"\\];

// Double-quoted string with standard escapes
STRING : '"' ( ~["\r\n] | ESCAPE )* '"' ;

// Number
NUMBER: [-]? [0-9]+;

// White space
WS: [ \r\n\t]+ -> skip;

path: ID ('::' ID)*;
paths: path | '[' (path (',' path)*)? ']';

ref: path '::' (ID | STRING);
refs: ref | '[' (ref (',' ref)*)? ']';

name: ID | STRING;

strings: STRING (',' STRING)*;
ids: ID (',' ID)*;

entity:
    'entity' ids ('in' paths)? ('='? recordType)? ('tags' type)? ';'
    | 'entity' ids 'enum' '[' strings ']' ';'
;

action:
    'action' name (',' name)? ('in' refs) 'appliesTo' '{'
        'principal' ':' paths ','
        'resource' ':' paths ','
        'context' ':' recordType
    '}' ';'
;

common:
    'type' ID  '=' type ';';

attribute: name '?'? ':' type;
recordType:
    '{' '}'
    | '{' attribute (',' attribute)* '}';
setType: 'Set' '<' type '>';
type: path | recordType | setType;

statement: entity | action | common;

namespace:
    'namespace' path '{' statement* '}';

schema:
    (statement | namespace)*;
