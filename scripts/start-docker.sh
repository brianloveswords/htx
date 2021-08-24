#!/usr/bin/env bash

set -euo pipefail

IMAGE=ghcr.io/graalvm/graalvm-ce:latest

docker run -it --rm \
    -v "$PWD"/wormhole:/wormhole \
    -v "$PWD"/htx-cli:/workspace/htx-cli \
    -v "$PWD"/htx-core:/workspace/htx-core \
    -v "$PWD"/htx-tests:/workspace/htx-tests \
    -v "$PWD"/project:/workspace/project \
    -v "$PWD"/build.sbt:/workspace/build.sbt \
    -v "$PWD"/sbt:/workspace/sbt \
    -w /workspace \
    $IMAGE bash
