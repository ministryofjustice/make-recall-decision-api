#!/bin/bash

set -euo pipefail

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly API_NAME=make-recall-decision-api
readonly API_DIR="${SCRIPT_DIR}/../../${API_NAME}"
readonly API_LOGFILE="/tmp/${API_NAME}.log"

npx kill-port 8080

pushd "${API_DIR}"
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun >>"${API_LOGFILE}" 2>&1 &
popd

function wait_for {
  printf "\n\nWaiting for %s to be ready.\n\n" "${2}"
  docker run --rm --network host docker.io/jwilder/dockerize -wait "${1}" -wait-retry-interval 2s -timeout 90s
}

wait_for "http://localhost:8080/health/readiness" "${API_NAME}"

echo ""
echo ""
echo "Logs for API can be found by running:"
echo "  tail -f ${API_LOGFILE}"
