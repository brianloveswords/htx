grammar Expr;
import CommonLexerRules;

// $antlr-format alignTrailingComments on, columnLimit 100, maxEmptyLinesToKeep 1, reflowComments off, useTab off, allowShortRulesOnASingleLine on, alignSemicolons ownLine

prog: stat+;
stat:
    expr NEWLINE          # printExpr
    | ID '=' expr NEWLINE # assign
    | NEWLINE             # blank
;
expr:
    expr op = ('*' | '/') expr   # MulDiv
    | expr op = ('+' | '-') expr # AddSub
    | INT                        # int
    | ID                         # id
    | '(' expr ')'               # parens
;

MUL: '*';
DIV: '/';
ADD: '+';
SUB: '-';
