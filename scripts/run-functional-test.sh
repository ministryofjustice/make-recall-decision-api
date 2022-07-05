#!/bin/bash

# start postgres db
#docker-compose -f ./docker-compose-postgres.yml up -d
#./scripts/wait-for-it.sh 127.0.0.1:5432 --strict -t 600

# start api
./scripts/start-local-development.sh &
./scripts/wait-for-it.sh 127.0.0.1:8080 --strict -t 600

# start functional test in separate jvm
./gradlew functional-test-light

# clean up
./scripts/clean-up-docker.sh
