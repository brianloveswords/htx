#!/usr/bin/env bash

set -euo pipefail

IMAGE=ghcr.io/graalvm/graalvm-ce:latest

docker run -it --rm \
    -v "$PWD"/wormhole:/wormhole \
    -v "$PWD"/htx-cli/src:/workspace/htx-cli/src \
    -v "$PWD"/htx-core/src:/workspace/htx-core/src \
    -v "$PWD"/htx-tests/src:/workspace/htx-tests/src \
    -v "$PWD"/project/plugins.sbt:/workspace/project/plugins.sbt \
    -v "$PWD"/build.sbt:/workspace/build.sbt \
    -v "$PWD"/sbt:/workspace/sbt \
    -w /workspace \
    $IMAGE bash
