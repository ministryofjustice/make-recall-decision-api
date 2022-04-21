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

pushd "${UI_DIR}"
docker compose down
popd

pushd "${API_DIR}"
docker compose down
popd

echo "Done."
