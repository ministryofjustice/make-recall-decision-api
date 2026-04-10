#!/usr/bin/env bash
set -xeuo pipefail

mkdir -p processing

cp ${DOMAIN_SRC} processing

plantuml processing
