#!/usr/bin/env bash
set -xeuo pipefail

pod_name=node

# Delete pod on script exit
function delete_pod() { kubectl delete pod "$pod_name"; }
trap delete_pod ERR SIGTERM SIGINT

# Start pod
kubectl run "$pod_name" \
  --image=library/node:22 \
  --restart=Never --stdin=true --tty=true \
  --overrides=" $(cat <<EOF
  {
    "spec": {
      "containers": [
        {
          "name": "node",
          "image": "library/node:22",
          "command": ["sh"],
          "stdin": true,
          "tty": true,
          "securityContext": { "runAsNonRoot": true, "runAsUser": 1000 },
          "resources": { "limits": { "cpu": "2000m", "memory": "2000Mi" } },
          "env": [{ "name": "HOST", "value": "${HOST}" },
						{ "name": "SCHEMA", "value": "${SCHEMA}" },
						{ "name": "DB", "value": "${DB}" },
						{ "name": "PORT", "value": "${PORT}" },
						{ "name": "DB_USERNAME", "value": "${DB_USERNAME}" },
						{ "name": "DB_PASSWORD", "value": "${DB_PASSWORD}" },
						{ "name": "SSL_CA_FILE", "value": "${SSL_CA_FILE}" },
						{ "name": "OUTPUT", "value": "${OUTPUT}" },
						{ "name": "METADATA", "value": "/tmp/create-data-dictionary/metadata.json" },
						{ "name": "IGNORETABLES", "value": "${IGNORETABLES}" }]
        }
      ]
    }
  }
EOF
)" -- sh & sleep 5

kubectl wait --for=condition=ready pod "$pod_name"

# Copy application files
kubectl cp create-data-dictionary "$pod_name:/tmp/create-data-dictionary" 

# Copy metadata file
kubectl cp ${METADATA} "$pod_name:/tmp/create-data-dictionary/metadata.json" 

# Run report
kubectl exec "$pod_name" -- node /tmp/create-data-dictionary/create-data-dictionary.js 

# Download report
kubectl cp "$pod_name:${OUTPUT}" data-dictionary-output

# Clean up
delete_pod
