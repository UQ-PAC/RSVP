#!/bin/bash

ERROR=
POLICY=photoapp-policy.cedar
ENTITIES=entities.json
SCHEMA=photoapp.cedarschema

authorise() {
  echo "$1 $2 $3"
  out=$(cedar authorize \
      --policies $POLICY \
      --entities $ENTITIES \
      --schema $SCHEMA \
      --principal "$1" \
      --action "$2" \
      --resource "$3" \
      --context context.json)

  out=$(echo $out)

  if [ "$out" == "$4" ]; then
      echo -e "\e[32m  OK [ "Expected: $out" ] \e[0m"
  else
      echo -e "\e[31m  FAIL [ "Expected: $4, got $out" ] \e[0m"
      ERROR=1
  fi
}

# View
echo "==== View ==="
authorise 'Account::"Alice"' 'Action::"viewPhoto"' 'Photo::"AliceMeadow"'   ALLOW # Anyone can see public photos
authorise 'Account::"Bob"'   'Action::"viewPhoto"' 'Photo::"AliceMeadow"'   ALLOW
authorise 'Account::"Carl"'  'Action::"viewPhoto"' 'Photo::"AliceMeadow"'   ALLOW
authorise 'Account::"Tim"'   'Action::"viewPhoto"' 'Photo::"AliceMeadow"'   ALLOW
authorise 'Account::"Alice"' 'Action::"viewPhoto"' 'Photo::"AliceHotel"'    ALLOW # Only friends or admins can see protected photos
authorise 'Account::"Bob"'   'Action::"viewPhoto"' 'Photo::"AliceHotel"'    ALLOW
authorise 'Account::"Carl"'  'Action::"viewPhoto"' 'Photo::"AliceHotel"'    DENY
authorise 'Account::"Tim"'   'Action::"viewPhoto"' 'Photo::"AliceHotel"'    ALLOW
authorise 'Account::"Alice"' 'Action::"viewPhoto"' 'Photo::"AlicePassport"' ALLOW # Owners or admins can see private photos
authorise 'Account::"Bob"'   'Action::"viewPhoto"' 'Photo::"AlicePassport"' DENY
authorise 'Account::"Carl"'  'Action::"viewPhoto"' 'Photo::"AlicePassport"' DENY
authorise 'Account::"Tim"'   'Action::"viewPhoto"' 'Photo::"AlicePassport"' ALLOW
echo

# Create
echo "==== Create ==="
authorise 'Account::"Alice"' 'Action::"createAlbum"' 'Account::"Alice"' ALLOW # Alice creates an account for herself
authorise 'Account::"Alice"' 'Action::"createAlbum"' 'Account::"Bob"'   DENY  # Alice cannot create album for Bob
authorise 'Account::"Tim"'   'Action::"createAlbum"' 'Account::"Tim"'   DENY  # Tim cannot create an album for himself
authorise 'Account::"Tim"'   'Action::"createAlbum"' 'Account::"Alice"' ALLOW # Tim can create album for Alice
echo

# Remove
echo "==== Remove ==="
authorise 'Account::"Alice"' 'Action::"removeAlbum"' 'Album::"AliceVacation"' ALLOW # Alice can remove her album
authorise 'Account::"Tim"'   'Action::"removeAlbum"' 'Album::"AliceVacation"' ALLOW # So can admin
authorise 'Account::"Bob"'   'Action::"removeAlbum"' 'Album::"AliceVacation"' DENY  # Another user cannot
echo

# Upload
echo "==== Upload ==="
authorise 'Account::"Alice"' 'Action::"uploadPhoto"' 'Album::"AliceVacation"' ALLOW # Users can upload photos to their albums
authorise 'Account::"Alice"' 'Action::"uploadPhoto"' 'Album::"BobVacation"'   DENY  # But not to others' albums
authorise 'Account::"Tim"'   'Action::"uploadPhoto"' 'Album::"BobVacation"'   DENY  # But not to others' albums (even admins)
echo

if [ -n "$ERROR" ]; then
    echo -e "\e[31m ERROR: There were errors \e[0m"
else
    echo -e "\e[32m All ok \e[0m"
fi
