#!/bin/bash
./scripts/clean-up-docker.sh
PID=$(lsof -n -i :8080 | grep LISTEN | awk '{ print $2; }')
kill $PID

