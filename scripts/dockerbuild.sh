#!/usr/bin/env bash

set -euo pipefail

sbt cli/nativeImage
cp htx-cli/target/native-image/cli /wormhole/htx.amd64.linux.static
upx /wormhole/htx.amd64.linux.static
