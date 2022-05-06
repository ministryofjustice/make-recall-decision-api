#!/bin/bash

# This script gets the token that the 'make-recall-decision-api' uses to connect to other HMPPS services.
# The token it generates can not be used to access the 'make-recall-decision-api' endpoints directly.

set -euo pipefail

ENV=dev

instructions() {
  echo "Usage: $0 <opts>" >&2
  echo " -h --> show usage" >&2
  echo " -e --> environment - allowed values are dev/preprod/prod (default=${ENV})"
}

while getopts :e:h option; do
  case "${option}" in
  h)
    instructions
    exit 0
    ;;
  e)
    ENV=${OPTARG}
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

# check for the ENV variable
set +u
if [[ ! "${ENV}" =~ ^(dev|preprod|prod)$ ]]; then
  echo "Invalid environment: ${ENV}" >&2
  instructions
  exit 1
fi
set -u

printf "\nGetting access token for ${ENV} environment...\n\n"

readonly SECRET_NAME=make-recall-decision-api
readonly NAMESPACE=make-recall-decision-$ENV

HMPPS_AUTH_URL=https://sign-in-$ENV.hmpps.service.justice.gov.uk
if [[ "${ENV}" == "prod" ]]; then
  HMPPS_AUTH_URL=https://sign-in.hmpps.service.justice.gov.uk
fi

secret=$(kubectl -n "${NAMESPACE}" get secret "${SECRET_NAME}" -o json)
client_id=$(jq -r '.data.SYSTEM_CLIENT_ID' <<<"${secret}" | base64 -d)
client_secret=$(jq -r '.data.SYSTEM_CLIENT_SECRET' <<<"${secret}" | base64 -d)

curl_command="
curl -s -X POST \"${HMPPS_AUTH_URL}/auth/oauth/token?grant_type=client_credentials\" \
  -H \"Content-Type: application/json\" \
  -H \"Authorization: Basic $(echo -n ${client_id}:${client_secret} | base64)\"
"

pod=$(kubectl -n "${NAMESPACE}" get pods | grep ${SECRET_NAME} | awk '{print $1}' | head -n 1)

kubectl -n "${NAMESPACE}" exec -it "${pod}" -- bash -c "${curl_command}" | jq -r '.access_token'
