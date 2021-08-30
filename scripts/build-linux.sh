#!/usr/bin/env bash

set -euo pipefail

IMAGE=graal

function main() {
    docker create -ti --name htx-builder \
        -v "$PWD"/wormhole:/wormhole \
        -v "$PWD"/htx-cli/src:/workspace/htx-cli/src \
        -v "$PWD"/htx-grammar/src:/workspace/htx-grammar/src \
        -v "$PWD"/htx-core/src:/workspace/htx-core/src \
        -v "$PWD"/htx-tests/src:/workspace/htx-tests/src \
        -v "$PWD"/scripts/dockerbuild.sh:/workspace/dockerbuild.sh \
        -v "$PWD"/project/lib:/workspace/project/lib \
        -v "$PWD"/project/bin:/workspace/project/bin \
        -v "$PWD"/project/plugins.sbt:/workspace/project/plugins.sbt \
        -v "$PWD"/project/Custom.scala:/workspace/project/Custom.scala \
        -v "$PWD"/build.sbt:/workspace/build.sbt \
        -v "$PWD"/sbt:/workspace/sbt \
        -w /workspace \
        $IMAGE bash dockerbuild.sh || true

    docker start -ai htx-builder
}

main
