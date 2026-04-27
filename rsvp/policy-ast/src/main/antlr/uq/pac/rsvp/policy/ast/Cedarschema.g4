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

TYPE: 'type';
ENTITY: 'entity';
ACTION: 'action';
APPLIES: 'appliesTo';
NAMESPACE: 'namespace';
PRINCIPAL: 'principal';
RESOURCE: 'resource';
CONTEXT: 'context';
ENUM: 'enum';
IN: 'in';

// Special list of keywords that are allowed to be used
// as record attribute names. For whatever reason the IN
// keyword cannot be an attribute name (as per the original cedar parser)
keywords:
    TYPE
    | ENTITY
    | ACTION
    | APPLIES
    | NAMESPACE
    | PRINCIPAL
    | RESOURCE
    | CONTEXT
    | ENUM
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

OPTIONAL: '?';

// Path: an entity type, a sequence of '::'-separated identifiers i.e., A or A::B
path: ident ('::' ident)*;
// A single path or a non-empty literal list of thereof
paths: path | '[' (path (',' path)*)? ']';

// Names are identifiers or strings
name: keywords | RESERVED | ID | STRING ;

// Comma-separated list of literal strings
strings: STRING (',' STRING)*;

attribute: name OPTIONAL? ':' type;
record:
    '{' '}'
    | '{' attribute (',' attribute)* ','? '}';

set: 'Set' '<' type '>';

type:
    path        # namedType
    | record    # recordType
    | set       # setType
;

// Action references: identifiers, strings or entity references with 'Action' sub-type
actionRef: name | (path '::')? STRING ;
actionRefs: actionRef | '[' actionRef (',' actionRef)* ']';

entityNames: ID (',' ID)*;
entity:
    ENTITY entityNames ('in' paths)? ('='? record)? ';'
    | ENTITY entityNames ENUM '[' strings ']' ';'
;

action:
    ACTION name (',' name)? (IN actionRefs)? appliesTo? ';'
;

appliesTo: APPLIES '{'
    PRINCIPAL ':' paths ','
    RESOURCE ':' paths
    (',' CONTEXT ':' record)?
'}';

typename: ID;
common:
    TYPE typename '=' type ';'
;

annotation: '@' ident ('(' STRING ')')?;

statement: annotation* (entity | action | common);

namespace:
    annotation* NAMESPACE path '{' statement* '}';

schema:
    (statement | namespace)*;
