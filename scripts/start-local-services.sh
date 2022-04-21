#!/bin/bash

set -euo pipefail

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly UI_NAME=make-recall-decision-ui
readonly UI_DIR="${SCRIPT_DIR}/../../${UI_NAME}"
readonly UI_LOGFILE="/tmp/${UI_NAME}.log"
readonly API_NAME=make-recall-decision-api
readonly API_DIR="${SCRIPT_DIR}/../../${API_NAME}"
readonly API_LOGFILE="/tmp/${API_NAME}.log"

npx kill-port 3000 3001 8080
pkill npm || true
pkill node || true

pushd "${UI_DIR}"
docker compose pull
docker compose build
docker compose up -d --scale=app=0
npm install
npm run start:dev >>"${UI_LOGFILE}" 2>&1 &
popd

pushd "${API_DIR}"
# TODO: uncomment the below when we have some dependency services to fire up...
# docker compose pull
# docker compose build
# docker compose up -d --scale=app=0
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun >>"${API_LOGFILE}" 2>&1 &
popd

function wait_for {
  printf "\n\nWaiting for ${2} to be ready."
  until curl -s --fail "${1}"; do
    printf "."
    sleep 2
  done
}

wait_for "http://localhost:9090/auth/health/ping" "hmpps-auth"
wait_for "http://localhost:3000/ping" "${UI_NAME}"
wait_for "http://localhost:8080/health/readiness" "${API_NAME}"

echo ""
echo ""
echo "Logs for API and UI can be found by running:"
echo "  tail -f ${API_LOGFILE}"
echo "  tail -f ${UI_LOGFILE}"
