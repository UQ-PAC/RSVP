#!/bin/bash

ERROR=
ENTITIES=petclinic-original-entities.json
POLICY=petclinic-original-policy.cedar
SCHEMA=petclinic-original-schema.cedarschema

TEST_1=ALLOW/visitor-findowner-findowners.json
TEST_2=ALLOW/visitor-findowner-home.json
TEST_3=ALLOW/visitor-findowner-owner.json
TEST_4=ALLOW/visitor-viewwebpage-home.json

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
        ERROR=1
        exit 1
    fi
    echo -e "\n"
}

echo "Schema Validation."
validate

echo "Principal: Visitor::\"Anonymous\"; Action: Action::\"FindOwner\"; Resource: WebPage::\"FindOwners\"."
authorise $TEST_1 ALLOW

echo "Principal: Visitor::\"Anonymous\"; Action: Action::\"FindOwner\"; Resource: WebPage::\"Home\"."
authorise $TEST_2 ALLOW

echo "Principal: Visitor::\"Anonymous\"; Action: Action::\"FindOwner\"; Resource: WebPage::\"Owner\"."
authorise $TEST_3 ALLOW

echo "Principal: Visitor::\"Anonymous\"; Action: Action::\"ViewWebPage\"; Resource: WebPage::\"Home\"."
authorise $TEST_4 ALLOW

if [ -n "$ERROR" ]; then
    echo -e "\e[31m ERROR: There were errors! \e[0m"
else
    echo -e "\e[32m ALL OK! \e[0m"
fi
