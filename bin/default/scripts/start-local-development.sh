#!/bin/bash

set -euo pipefail

./gradlew --stop
./gradlew clean
SYSTEM_CLIENT_ID=$SYSTEM_CLIENT_ID SYSTEM_CLIENT_SECRET=$SYSTEM_CLIENT_SECRET SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun