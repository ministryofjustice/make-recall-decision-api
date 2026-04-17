#!/usr/bin/env bash
set -xeuo pipefail

mkdir -p processing

cp ${ERD_SRC} processing

plantuml processing
