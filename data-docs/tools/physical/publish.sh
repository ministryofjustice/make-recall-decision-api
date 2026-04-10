#!/usr/local/bin/bash
set -euo pipefail

set -x

BASE_URL="https://dsdmoj.atlassian.net/wiki"
IMAGE_DIR="schema-spy-report/diagrams/tables"

command -v jq >/dev/null || {
  echo "jq is required"; exit 1;
}

command -v jq >/dev/null || { echo "jq required"; exit 1; }

# ---- fetch page info ----
PAGE_JSON=$(curl -s -u "${ATLASSIAN_USER}:${ATLASSIAN_API_TOKEN}" \
  "$BASE_URL/rest/api/content/$PHYSICAL_PAGE_ID?expand=version,title")

TITLE=$(jq -r '.title' <<<"$PAGE_JSON")
VERSION=$(jq -r '.version.number' <<<"$PAGE_JSON")
NEXT_VERSION=$((VERSION + 1))

# ---- upload images ----
IMAGE_MACROS=""

for img in "$IMAGE_DIR"/*.png; do
  [ -e "$img" ] || continue
  NAME=$(basename "$img")

  echo "Uploading $NAME"

  curl -s -X POST -u "${ATLASSIAN_USER}:${ATLASSIAN_API_TOKEN}" \
    -H "X-Atlassian-Token: no-check" \
    -F "file=@$img" \
    "$BASE_URL/rest/api/content/$PHYSICAL_PAGE_ID/child/attachment" \
    >/dev/null

  IMAGE_MACROS+=$'\n'"<ac:image><ri:attachment ri:filename=\"$NAME\"/></ac:image>"
done

BODY="<p><strong>This page is automatically generated. Do not manually edit.</strong></p>${IMAGE_MACROS}"

# ---- JSON encode storage body safely ----
ENCODED_BODY=$(jq -Rs . <<<"$BODY")

# ---- update page (storage format) ----
curl -s -X PUT -u "${ATLASSIAN_USER}:${ATLASSIAN_API_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"id\": \"$PHYSICAL_PAGE_ID\",
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
  "$BASE_URL/rest/api/content/$PHYSICAL_PAGE_ID" \
  >/dev/null

echo "Page updated using storage format"

