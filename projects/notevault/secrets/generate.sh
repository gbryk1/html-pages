#!/bin/sh
# Tworzy losowe hasła dla compose.full.yaml. Pliki *.txt są w .gitignore: nigdy ich nie commituj.
set -eu
cd "$(dirname "$0")"
for name in db_password redis_password admin_password kc_admin_password; do
  if [ ! -f "$name.txt" ]; then
    openssl rand -base64 24 | tr -d '\n/+=' > "$name.txt"
    chmod 600 "$name.txt"
    echo "created secrets/$name.txt"
  fi
done
