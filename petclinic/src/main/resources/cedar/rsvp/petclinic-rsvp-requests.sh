#!/bin/bash

ERROR=
ENTITIES=petclinic-rsvp-entities.json
POLICY=petclinic-rsvp-policy.cedar
SCHEMA=petclinic-rsvp-schema.cedarschema

validate() {
    out=$(cedar validate \
        --schema $SCHEMA \
        --policies $POLICY)
    
    echo $out
    echo -e "\n"
}

authorise() {
    echo "$1"
    out=$(cedar authorize \
        --policies $POLICY \
        --schema $SCHEMA \
        --entities $ENTITIES \
        --request-json $1)

    out=$(echo $out)

    if [ "$out" == "$2" ]; then
        echo -e "\e[32m OK [ "Expected: $out". ] \e[0m"
    else
        echo -e "\e[31m FAIL [ "Expected: $2, got $out". ] \e[0m"
    fi
    echo -e "\n"
}

echo "Schema Validation."
validate

echo "ALLOW Tests."
for file in "./ALLOW"/*
do
    authorise $file ALLOW
done

echo "DENY Tests."
for file in "./DENY"/*
do
    authorise $file DENY
done

if [ -n "$ERROR" ]; then
    echo -e "\e[31m ERROR: There were errors! \e[0m"
else
    echo -e "\e[32m ALL OK! \e[0m"
fi
