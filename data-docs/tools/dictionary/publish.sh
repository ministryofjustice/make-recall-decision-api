#!/usr/local/bin/bash
set -xeuo pipefail

BASE_URL="https://dsdmoj.atlassian.net/wiki"
DICTIONARY_DIR="data-dictionary-output"

command -v jq >/dev/null || {
  echo "jq is required"; exit 1;
}

command -v jq >/dev/null || { echo "jq required"; exit 1; }

# ---- fetch page info ----
PAGE_JSON=$(curl -s -u "${ATLASSIAN_USER}:${ATLASSIAN_API_TOKEN}" \
  "${BASE_URL}/rest/api/content/${DICTIONARY_PAGE_ID}?expand=version,title")

TITLE=$(jq -r '.title' <<<"$PAGE_JSON")
VERSION=$(jq -r '.version.number' <<<"$PAGE_JSON")
NEXT_VERSION=$((VERSION + 1))


BODY=$(cat ${DICTIONARY_DIR}/data-dictionary.html)

BODY="<p><strong>This page is automatically generated. Do not manually edit.</strong></p>${BODY}"

# ---- JSON encode storage body safely ----
ENCODED_BODY=$(jq -Rs . <<<"$BODY")

# ---- update page (storage format) ----
curl -s -X PUT -u "${ATLASSIAN_USER}:${ATLASSIAN_API_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"id\": \"$DICTIONARY_PAGE_ID\",
    \"type\": \"page\",
    \"title\": \"$TITLE\",
    \"version\": { \"number\": $NEXT_VERSION },
    \"body\": {
      \"storage\": {
        \"representation\": \"storage\",
        \"value\": $ENCODED_BODY
      }
    }
  }" \
  "$BASE_URL/rest/api/content/$DICTIONARY_PAGE_ID" \
  >/dev/null

echo "Page updated using storage format"

curl -s -u "${ATLASSIAN_USER}:${ATLASSIAN_API_TOKEN}" \
  "${BASE_URL}/rest/api/content/${DICTIONARY_PAGE_ID}"
