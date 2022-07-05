#!/bin/bash

set -euo pipefail

./gradlew --stop
./gradlew clean
SYSTEM_CLIENT_ID="bill-sclater" SYSTEM_CLIENT_SECRET="KbICZKm61'xNx+9y5Z63IvA-7(ohdu&Ubg0LF0m%\$q0suoF<P8S3E\$QV*;5h" SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun