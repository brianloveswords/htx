#!/usr/bin/env bash

set -euo pipefail

ASSEMBLY_JAR=htx-cli/target/scala-3.0.1/htx.jar
CONFIG_PATH=htx-cli/src/main/resources/META-INF/native-image
OUTPUT_MODE=output

java -agentlib:native-image-agent=config-$OUTPUT_MODE-dir=$CONFIG_PATH \
    -jar $ASSEMBLY_JAR \
    https://example.com "{title}: {@}"
