#!/usr/bin/env bash

ERROR=
SCHEMA="childclinic-rsvp-schema.cedarschema"
ENTITIES="childclinic-rsvp-entities.json"
POLICY="childclinic-rsvp-policy.cedar"

ALLOW="./ALLOW"
DENY="./DENY"
VERBOSE=false

counter=0
AtoD=0
DtoA=0
err=0

while getopts "htvp:" opt; do
  case $opt in
    h)
      echo "Usage: $0 [-h] [-t] [-v] [-p another-policy.cedar]" >&2
      exit 0
      ;;
    t)
      ALLOW="./ALLOW-TEST"
      DENY="./DENY-TEST"
      ;;
    v)
      VERBOSE=true
      ;;
    p)
      POLICY="$OPTARG"
      ;;
    \?)
      echo "Invalid option: -$OPTARG" >&2
      exit 1
      ;;
    :)
      echo "Option -$OPTARG requires an argument." >&2
      exit 1
      ;;
  esac
done

shift $((OPTIND -1))

validate() {
    out=$(cedar validate \
        --schema $SCHEMA \
        --policies $POLICY)
    
    echo $out
}

authorise() {
    echo "$1"
    if [[ "$VERBOSE" == true ]]; then
        cat "$1"
    fi

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
        if [[ "$2" == "ALLOW" && "${result[0]}" == "DENY" ]]; then
          ((AtoD++))
        elif [[ "$2" == "DENY" && "${result[0]}" == "ALLOW" ]]; then
          ((DtoA++))
        else
          ((err++))
        fi
    fi
    ((counter++))

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

if [[ -d "$ALLOW" ]]; then
    echo -e "\e[1;34m ALLOW Tests. \e[0m"
    for file in "$ALLOW"/*; do
        authorise $file ALLOW
    done
    echo -e "\n"
else
    echo -e "\e[1;34m No ALLOW tests defined. \n\e[0m"
fi

if [[ -d "$DENY" ]]; then
    echo -e "\e[1;34m DENY Tests. \e[0m"
    for file in "$DENY"/*; do
        authorise $file DENY
    done
    echo -e "\n"
else
    echo -e "\e[1;34m No DENY tests defined. \n\e[0m"
fi

if [[ -n "$ERROR" ]]; then
    echo -e "\e[1;31m ERROR: There were failed tests! \e[0m"
    echo -e "\e[1;31m ALLOW -> DENY: $AtoD. \e[0m"
    echo -e "\e[1;31m DENY -> ALLOW: $DtoA. \e[0m"
    echo -e "\e[1;31m TOTAL: $counter. \e[0m"
    echo -e "\e[1;31m ERROR: $err. \e[0m"
else
    echo -e "\e[1;32m ALL OK! \e[0m"
fi
