#!/bin/bash

set -euo pipefail

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly UI_NAME=make-recall-decision-ui
readonly UI_DIR="${SCRIPT_DIR}/../../${UI_NAME}"
readonly API_NAME=make-recall-decision-api
readonly API_DIR="${SCRIPT_DIR}/../../${API_NAME}"

npx kill-port 3000 3001 8080
pkill npm || true
pkill node || true

pushd "${API_DIR}"
printf "\n\nStopping API components...\n\n"
docker compose down || true # ignore the network error, this will be cleared up by the next step
popd

pushd "${UI_DIR}"
printf "\n\nStopping UI components...\n\n"
docker compose down
popd

printf "\n\nAll services are stopped.\n\n"
