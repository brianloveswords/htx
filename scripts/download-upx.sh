#!/usr/bin/env bash

UPX_VERSION=3.96

set -euo pipefail

mkdir -p tmp/upx && cd tmp/upx
curl -LO https://github.com/upx/upx/releases/download/v${UPX_VERSION}/upx-${UPX_VERSION}-amd64_linux.tar.xz
curl -LO https://github.com/upx/upx/releases/download/v${UPX_VERSION}/upx-${UPX_VERSION}-win64.zip

# for macOS, `brew install upx` then copy what gets installed
