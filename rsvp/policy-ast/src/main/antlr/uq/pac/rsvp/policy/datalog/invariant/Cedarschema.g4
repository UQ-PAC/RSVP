grammar Cedarschema;

RESERVED:
    'Bool'
    | 'Boolean'
    | 'Long'
    | 'String'
    | 'Set'
    | 'Record'
    | 'Entity'
    | 'Extension'
    // The below are allowed in Cedar, but prevented here to avoid shadowing
    | 'Action'
    | 'ipaddr'
    | 'duration'
    | 'datetime'
    | 'decimal'
;

// Identifier (excluding keywords)
ID: [_A-Za-z][A-Za-z0-9_]*;

// Identifier (including reserved keywords)
ident: RESERVED | ID;

// Comments
COMMENT: '//' ~[\r\n]* -> skip;

fragment ESCAPE: '\\' [tbnr"\\];

// Double-quoted string with standard escapes
STRING : '"' ( ~["\r\n] | ESCAPE )* '"' ;

// Number
NUMBER: [-]? [0-9]+;

// White space
WS: [ \r\n\t]+ -> skip;

// Path: an entity type, a sequence of '::'-separated identifiers i.e., A or A::B
path: ident ('::' ident)*;
// A single path or a non-empty literal list of thereof
paths: path | '[' (path (',' path)*)? ']';

// Names are identifiers or strings
name: ID | STRING;
// Comma-separated list of literal strings
strings: STRING (',' STRING)*;

// Action references: identifiers, strings or entity references with 'Action' sub-type
actionRef: name | (path '::')? 'Action' '::' STRING ;
actionRefs: actionRef | '[' actionRef (',' actionRef)* ']';

entityNames: ID (',' ID)*;
entity:
    'entity' entityNames ('in' paths)? ('='? recordType)? ';'
    | 'entity' entityNames 'enum' '[' strings ']' ';'
;

action:
    'action' name (',' name)? ('in' actionRefs)? appliesTo? ';'
;

appliesTo: 'appliesTo' '{'
    'principal' ':' paths ','
    'resource' ':' paths
    (',' 'context' ':' recordType)?
'}';

typename: ID;
common:
    'type' typename '=' type ';'
;

attribute: name '?'? ':' type;
recordType:
    '{' '}'
    | '{' attribute (',' attribute)* '}';
setType: 'Set' '<' type '>';
type: path | recordType | setType;

annotation: '@' ident ('(' STRING ')')?;

statement: annotation* (entity | action | common);

namespace:
    annotation* 'namespace' path '{' statement* '}';

schema:
    (statement | namespace)*;
