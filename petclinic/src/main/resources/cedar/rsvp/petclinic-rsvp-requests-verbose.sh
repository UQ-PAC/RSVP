#!/usr/bin/env bash

ERROR=
ENTITIES=petclinic-rsvp-entities.json
POLICY=petclinic-rsvp-policy.cedar
SCHEMA=petclinic-rsvp-schema.cedarschema

validate() {
    out=$(cedar validate \
        --schema $SCHEMA \
        --policies $POLICY)
    
    echo $out
}

authorise() {
    echo "$1"

    mapfile -t out < <(cedar authorize \
                        --verbose \
                        --policies $POLICY \
                        --schema $SCHEMA \
                        --entities $ENTITIES \
                        --request-json $1 \
                        | sed -e 's/\r//g' \
                        -e 's/^[[:space:]]*//' \
                        -e 's/[[:space:]]*$//')

    declare -a result=()

    for entry in "${out[@]}"; do
        if [[ "$entry" =~ ^policy[[:digit:]]+$|^ALLOW$|^DENY$|^Policy:.*\.$ ]]; then
            temp=$(sed -e 's/^Policy\:[[:space:]]/\"/' -e 's/\.$/\"/' <<< "$entry")
            result+=("$temp")
        fi
    done

    # for index in "${!result[@]}"; do
    #     printf "[%d]: %s\n" "$index" "${result[$index]}"
    # done

    if [[ "${result[0]}" == "$2" ]]; then
        echo -e "\e[1;32m OK [ "Expected: $2. Received: ${result[0]}". ] \e[0m"
    else
        echo -e "\e[1;31m FAIL [ "Expected: $2. Received: ${result[0]}". ] \e[0m"
        ERROR=1
    fi

    total="${#result[@]}"

    if [[ "$total" == "1" ]]; then
        printf "%b No policies applied! Decision based on Cedar's default DENY. %b\n" "\033[1;34m" "\033[0m"
    else
        original_ifs="$IFS"
        IFS=";"
        printf "%b Decision based on policies: %s. %b\n" "\033[1;34m" "${result[*]:1}" "\033[0m"
        IFS="$original_ifs"
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
