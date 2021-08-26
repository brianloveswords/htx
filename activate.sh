#!/usr/bin/env bash

export CLASSPATH=".:$PWD/lib/antlr-4.9.2-complete.jar::$CLASSPATH"
alias antlr4="java org.antlr.v4.Tool"
alias grun="java org.antlr.v4.gui.TestRig"
