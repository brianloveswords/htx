#!/usr/bin/env bash

## this is mounted in the builder image and intended to run from there

set -euo pipefail

function main() {
    if [ "$PWD" = "/workspace" ]; then
        sbt cli/nativeImage
        cp htx-cli/target/native-image/cli /wormhole/htx.amd64.linux.static
        upx /wormhole/htx.amd64.linux.static
        exit 0
    fi
    echo "must be run from /workspace"
    exit 1
}

main
