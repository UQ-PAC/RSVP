#!/bin/bash

POLICY=policy.cedar
ENTITIES=entities.json
SCHEMA=schema.cedarschema

if [ "$1" = "validate" ]; then
    cedar validate --schema $SCHEMA --policies $POLICY || exit 1
    exit 0
fi

authorise() {
  echo "request($1, $2, $3)"
  out=$(cedar authorize \
      --policies $POLICY \
      --entities $ENTITIES \
      --principal "$1" \
      --schema $SCHEMA \
      --action "$2" \
      --resource "$3")

     out=$(echo $out)
     echo "  ": $out
}

authorise 'User::"Alice"' 'Action::"View"' 'Photo::"clowder"'
authorise 'User::"Bob"'   'Action::"View"' 'Photo::"clowder"'
authorise 'User::"Carl"'  'Action::"View"' 'Photo::"clowder"'
authorise 'Admin::"Tim"'  'Action::"View"' 'Photo::"clowder"'

echo ========

authorise 'User::"Alice"' 'Action::"View"' 'Photo::"pack"'
authorise 'User::"Bob"'   'Action::"View"' 'Photo::"pack"'
authorise 'User::"Carl"'  'Action::"View"' 'Photo::"pack"'
authorise 'Admin::"Tim"'  'Action::"View"' 'Photo::"pack"'

echo ========

authorise 'User::"Alice"' 'Action::"View"' 'Photo::"gathering"'
authorise 'User::"Bob"'   'Action::"View"' 'Photo::"gathering"'
authorise 'User::"Carl"'  'Action::"View"' 'Photo::"gathering"'
authorise 'Admin::"Tim"'  'Action::"View"' 'Photo::"gathering"'

echo ========

authorise 'User::"Alice"' 'Action::"Look"' 'Photo::"gathering"'
authorise 'Admin::"Tim"'  'Action::"Look"' 'Photo::"gathering"'

exit 0
