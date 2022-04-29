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

pushd "${API_DIR}"
printf "\n\nBuilding/starting API components...\n\n"
docker compose pull
docker compose up -d --scale=${API_NAME}=0
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun >>"${API_LOGFILE}" 2>&1 &
popd

pushd "${UI_DIR}"
printf "\n\nBuilding/starting UI components...\n\n"
docker compose pull
docker compose up -d --scale=${UI_NAME}=0
npm install
npm run start:dev >>"${UI_LOGFILE}" 2>&1 &
popd

function wait_for {
  printf "\n\nWaiting for %s to be ready.\n\n" "${2}"
  local TRIES=0
  until curl -s --fail "${1}"; do
    printf "."
    ((TRIES++))

    if [ $TRIES -gt 50 ]; then
      printf "Failed to start %s after 50 tries." "${2}"
      exit 1
    fi

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
