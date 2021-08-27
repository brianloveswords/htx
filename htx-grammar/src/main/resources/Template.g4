grammar Template;

// $antlr-format alignTrailingComments on, columnLimit 100, maxEmptyLinesToKeep 1, reflowComments off, useTab off, allowShortRulesOnASingleLine off, alignSemicolons ownLine

template:
    part+
;

part:
    pattern
    | text
;

pattern:
    PATTERN
;
text:
    TEXT
;

// lexer

PATTERN:
    '{' (PATTERN_END_ESC | STRING | .)+? '}'
;

TEXT:
    (PATTERN_START_ESC | ~('{' | '"' | '\''))+
;

fragment PATTERN_START_ESC:
    '\\{'
;

fragment PATTERN_END_ESC:
    '\\}'
;

// 2-char sequences \", \', and \\
fragment STRING_ESC:
    '\\"'
    | '\\\''
    | '\\\\'
;

fragment STRING:
    '"' (STRING_ESC | .)*? '"'
    | '\'' (STRING_ESC | .)*? '\''
;

