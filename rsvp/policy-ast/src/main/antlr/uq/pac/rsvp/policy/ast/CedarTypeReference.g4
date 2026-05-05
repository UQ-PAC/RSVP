grammar CedarTypeReference;

// Identifier (excluding keywords)
ID: [_A-Za-z][A-Za-z0-9_]*;

fragment ESCAPE: '\\' [tbnr"\\];

// Double-quoted string with standard escapes
STRING : '"' ( ~["\r\n] | ESCAPE )* '"' ;

// Path: an entity type, a sequence of '::'-separated identifiers i.e., A or A::B
path: ID ('::' ID)*;

reference: path ('::' STRING)?;
