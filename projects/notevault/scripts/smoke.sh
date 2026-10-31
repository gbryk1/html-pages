#!/bin/sh
# Test dymny całego systemu z compose.full.yaml: każda linia to jedna obrona, która ma zadziałać.
# Wymaga: curl, jq. Uruchom po `docker compose -f compose.full.yaml up -d --build`.
set -eu
APP=${APP:-http://localhost:8080}
KC=${KC:-http://localhost:8180/realms/notevault}
JAR=$(mktemp)
BOB=$(mktemp)
trap 'rm -f "$JAR" "$BOB"' EXIT

check() { # check <opis> <oczekiwany kod> <faktyczny kod>
  if [ "$2" = "$3" ]; then echo "OK    $1 ($3)"; else echo "FAIL  $1: oczekiwano $2, jest $3"; FAILED=1; fi
}
code() { curl -s -o /dev/null -w '%{http_code}' "$@"; }
csrf() { awk '$6=="XSRF-TOKEN"{print $7}' "$1"; }
login() { # login <jar> <user> <hasło>
  curl -s -o /dev/null -c "$1" -b "$1" "$APP/api/csrf"
  code -c "$1" -b "$1" -H "X-XSRF-TOKEN: $(csrf "$1")" -d "username=$2&password=$3" "$APP/api/login"
}
register() { # register <jar> <user> <hasło>
  curl -s -o /dev/null -c "$1" -b "$1" "$APP/api/csrf"
  code -c "$1" -b "$1" -H "X-XSRF-TOKEN: $(csrf "$1")" -H 'Content-Type: application/json' \
    -d "{\"username\":\"$2\",\"password\":\"$3\"}" "$APP/api/register"
}
FAILED=0
SUFFIX=$(date +%s)
ALICE="alice$SUFFIX"
BOBN="bob$SUFFIX"

check "health publiczny" 200 "$(code "$APP/actuator/health")"
check "API bez logowania" 401 "$(code "$APP/api/notes")"
check "rejestracja z krótkim hasłem" 400 "$(register "$JAR" "$ALICE" short)"
check "rejestracja" 201 "$(register "$JAR" "$ALICE" alice-password-123)"
check "logowanie bez tokenu CSRF" 403 "$(code -d "username=$ALICE&password=alice-password-123" "$APP/api/login")"
check "logowanie" 204 "$(login "$JAR" "$ALICE" alice-password-123)"
check "nowa notatka" 201 "$(code -c "$JAR" -b "$JAR" -H "X-XSRF-TOKEN: $(csrf "$JAR")" -H 'Content-Type: application/json' \
  -d '{"title":"PIN do sejfu","body":"1234"}' "$APP/api/notes")"
NOTE=$(curl -s -b "$JAR" "$APP/api/notes" | jq '.[0].id')
check "Alice czyta swoją notatkę" 200 "$(code -b "$JAR" "$APP/api/notes/$NOTE")"
register "$BOB" "$BOBN" bob-password-1234 > /dev/null
login "$BOB" "$BOBN" bob-password-1234 > /dev/null
check "IDOR: Bob czyta notatkę Alice" 404 "$(code -b "$BOB" "$APP/api/notes/$NOTE")"
check "Bob w panelu admina" 403 "$(code -b "$BOB" "$APP/api/admin/users")"
check "admin z hasłem z Docker secret" 204 "$(login "$(mktemp)" admin "$(cat secrets/admin_password.txt)")"

TOKEN=$(curl -s -d grant_type=password -d client_id=notevault-cli -d username=alice -d password=alice-dev-password \
  "$KC/protocol/openid-connect/token" | jq -r .access_token)
check "API /ext bez tokenu" 401 "$(code "$APP/ext/notes")"
check "API /ext z tokenem z Keycloaka" 200 "$(code -H "Authorization: Bearer $TOKEN" "$APP/ext/notes")"
check "API /ext/admin bez roli admin" 403 "$(code -H "Authorization: Bearer $TOKEN" "$APP/ext/admin/stats")"

exit $FAILED
