#!/bin/bash

set -euo pipefail

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly UI_NAME=make-recall-decision-ui
readonly UI_DIR="${SCRIPT_DIR}/../../${UI_NAME}"
readonly UI_LOGFILE="/tmp/${UI_NAME}.log"

npx kill-port 3000 3001
pkill npm || true
pkill node || true

pushd "${UI_DIR}"
npm install
npm run start:dev >>"${UI_LOGFILE}" 2>&1 &
popd

function wait_for {
  printf "\n\nWaiting for %s to be ready.\n\n" "${2}"
  docker run --rm --network host docker.io/jwilder/dockerize -wait "${1}" -wait-retry-interval 2s -timeout 120s
}

wait_for "http://localhost:3000/ping" "${UI_NAME}"

echo ""
echo ""
echo "Logs for UI can be found by running:"
echo "  tail -f ${UI_LOGFILE}"
