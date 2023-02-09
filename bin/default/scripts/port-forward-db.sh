#!/bin/bash
set -euo pipefail

function usage {
  echo "
./$(basename $0) [option]

Options:
    -h --> show usage
    -e --> environment (REQUIRED) - allowed values: 'dev' or 'preprod'
  "
}

function check_dep {
  if ! command -v "${1}" &>/dev/null; then
    echo "You need '${1}' - '${2}'"
    exit 1
  fi
}

# Check dependencies
check_dep "psql" "brew install postgresql"
check_dep "jq" "brew install jq"
check_dep "kubectl" "asdf install kubectl 1.19.15"

# get cli options
while getopts :e:h opt; do
  case ${opt} in
  e) ENV=${OPTARG} ;;
  h)
    usage
    exit
    ;;
  \?)
    echo "Unknown option: -${OPTARG}" >&2
    exit 1
    ;;
  :)
    echo "Missing option argument for -${OPTARG}" >&2
    exit 1
    ;;
  *)
    echo "Unimplemented option: -${OPTARG}" >&2
    exit 1
    ;;
  esac
done

# check for the ENV variable
set +u
if [[ ! "${ENV}" =~ ^(dev|preprod)$ ]]; then
  usage
  exit 1
fi
set -u

K8S_NAMESPACE="make-recall-decision-${ENV}"
PFP_NAME="make-recall-decision-api-db-proxy-$(whoami | tr ._ -)"
SECRET=make-recall-decision-api-database

DB_HOST=$(kubectl -n "${K8S_NAMESPACE}" get secret "${SECRET}" -o json | jq -r '.data.host | @base64d')
DB_USER=$(kubectl -n "${K8S_NAMESPACE}" get secret "${SECRET}" -o json | jq -r '.data.username | @base64d')
DB_PASS=$(kubectl -n "${K8S_NAMESPACE}" get secret "${SECRET}" -o json | jq -r '.data.password | @base64d')
LOCAL_DB_PORT=5433
REMOTE_DB_PORT=5432

echo "Connecting to ${K8S_NAMESPACE}"

set +e
PFP_COUNT=$(kubectl -n "${K8S_NAMESPACE}" get pods | grep -c "${PFP_NAME}")
set -e

if [ "${PFP_COUNT}" -eq "0" ]; then
  echo "Starting up port forward pod..."

  kubectl -n "${K8S_NAMESPACE}" run \
    "${PFP_NAME}" \
    --image=ministryofjustice/port-forward \
    --port="${REMOTE_DB_PORT}" \
    --env="REMOTE_HOST=${DB_HOST}" \
    --env="LOCAL_PORT=${REMOTE_DB_PORT}" \
    --env="REMOTE_PORT=${REMOTE_DB_PORT}"

  sleep 5
fi

set +e
cmd="kubectl -n ${K8S_NAMESPACE} port-forward ${PFP_NAME} ${LOCAL_DB_PORT}:${REMOTE_DB_PORT}"
pid=$(pgrep -f "${cmd}")
set -e

echo "You can now connect to the database on 127.0.0.1:${LOCAL_DB_PORT} - username: ${DB_USER} - password: ${DB_PASS}"

# if the port-forward isn't already running, start it...
if [[ -z "${pid}" ]]; then
  ${cmd}
fi
