#!/usr/bin/env bash

ERROR=
ENTITIES=petclinic-original-entities.json
POLICY=petclinic-original-policy.cedar
SCHEMA=petclinic-original-schema.cedarschema

validate() {
    out=$(cedar validate \
        --schema $SCHEMA \
        --policies $POLICY)

    echo $out
}

authorise() {
    echo "$1"

    out=$(cedar authorize \
        --policies $POLICY \
        --schema $SCHEMA \
        --entities $ENTITIES \
        --request-json $1)

    out=$(echo $out)

    if [[ "$out" == "$2" ]]; then
        echo -e "\e[1;32m OK [ "Expected: $2. Received: $out". ] \e[0m"
    else
        echo -e "\e[1;31m FAIL [ "Expected: $2. Received: $out". ] \e[0m"
        ERROR=1
    fi
}

echo -e "\e[1;34m Schema Validation. \e[0m"
validate
echo -e "\n"

if [[ -d "./ALLOW" ]]; then
    echo -e "\e[1;34m ALLOW Tests. \e[0m"
    for file in "./ALLOW"/*; do
        authorise $file ALLOW
    done
    echo -e "\n"
else
    echo -e "\e[1;34m No ALLOW tests defined. \n\e[0m"
fi

if [[ -d "./DENY" ]]; then
    echo -e "\e[1;34m DENY Tests. \e[0m"
    for file in "./DENY"/*; do
        authorise $file DENY
    done
    echo -e "\n"
else
    echo -e "\e[1;34m No DENY tests defined. \n\e[0m"
fi

if [[ -n "$ERROR" ]]; then
    echo -e "\e[1;31m ERROR: There were failed tests! \e[0m"
else
    echo -e "\e[1;32m ALL OK! \e[0m"
fi
