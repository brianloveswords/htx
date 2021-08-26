#!/usr/bin/env bash

export CLASSPATH=".:$PWD/project/lib/antlr-4.9.2-complete.jar:$PWD/htx-grammar/target/scala-3.0.1/classes:$CLASSPATH"
alias antlr4="java org.antlr.v4.Tool"
alias grun="java org.antlr.v4.gui.TestRig"
