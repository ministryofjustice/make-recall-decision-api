#!/bin/bash

set -euo pipefail

BUILD_HMPPS_AUTH=false
RUN_DOCKER_COMPOSE_PULL=false

instructions() {
  echo "Usage: $0 <opts>" >&2
  echo " -h --> show usage" >&2
  echo " -a --> build hmpps-auth - needed for M1 macs (default=${BUILD_HMPPS_AUTH})" >&2
  echo " -p --> run docker compose pull (default=${RUN_DOCKER_COMPOSE_PULL})" >&2
}

while getopts ":h:ap" option; do
  case "${option}" in
  h)
    instructions
    exit 0
    ;;
  a)
    BUILD_HMPPS_AUTH=true
    ;;
  p)
    RUN_DOCKER_COMPOSE_PULL=true
    ;;
  \?)
    echo "Option '-$OPTARG' is not a valid option." >&2
    instructions
    exit 1
    ;;
  :)
    echo "Option '-$OPTARG' needs an argument." >&2
    instructions
    exit 1
    ;;
  esac
done

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly AUTH_NAME=hmpps-auth
readonly AUTH_DIR="${SCRIPT_DIR}/../../${AUTH_NAME}"
readonly UI_NAME=make-recall-decision-ui
readonly UI_DIR="${SCRIPT_DIR}/../../${UI_NAME}"
readonly UI_LOGFILE="/tmp/${UI_NAME}.log"
readonly API_NAME=make-recall-decision-api
readonly API_DIR="${SCRIPT_DIR}/../../${API_NAME}"
readonly API_LOGFILE="/tmp/${API_NAME}.log"

npx kill-port 3000 3001 8080
pkill npm || true
pkill node || true

if [[ "${RUN_DOCKER_COMPOSE_PULL}" == "true" ]]; then
  printf "\n\nRunning 'docker compose pull' on all services...\n\n"
  docker compose -f "${UI_DIR}/docker-compose.yml" pull
  docker compose -f "${API_DIR}/docker-compose.yml" pull
fi

if [[ "${BUILD_HMPPS_AUTH}" == "true" ]]; then
  printf "\n\nBuilding hmpps-auth\n\n"
  pushd "${AUTH_DIR}"
  git pull origin main
  docker build . --tag ghcr.io/ministryofjustice/hmpps-auth:latest
  popd
fi

pushd "${API_DIR}"
printf "\n\nBuilding/starting API components...\n\n"
docker compose up -d --scale=${API_NAME}=0
./gradlew bootRun >>"${API_LOGFILE}" 2>&1 &
popd

pushd "${UI_DIR}"
printf "\n\nBuilding/starting UI components...\n\n"
docker compose up -d --scale=${UI_NAME}=0
npm install
npm run start:dev >>"${UI_LOGFILE}" 2>&1 &
popd

function wait_for {
  printf "\n\nWaiting for %s to be ready.\n\n" "${2}"
  docker run --rm --network host docker.io/jwilder/dockerize -wait "${1}" -wait-retry-interval 2s -timeout 120s
}

wait_for "http://localhost:9090/auth/health/ping" "${AUTH_NAME}"
wait_for "http://localhost:3000/ping" "${UI_NAME}"
wait_for "http://localhost:8080/health/readiness" "${API_NAME}"
wait_for "http://localhost:9091/health" "gotenberg"

printf "\n\nAll services started.\n\n"
printf "\n\nLogs for API and UI can be found by running:\n"
echo "  tail -f ${API_LOGFILE}"
echo "  tail -f ${UI_LOGFILE}"
