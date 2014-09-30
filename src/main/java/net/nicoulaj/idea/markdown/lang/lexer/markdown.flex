package net.nicoulaj.idea.markdown.lang.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import net.nicoulaj.idea.markdown.lang.MarkdownTokenTypes;

import java.util.Stack;

/* Auto generated File */
%%

%class _MarkdownLexer
%implements FlexLexer
%unicode
%public
%column

%function advance
%type IElementType

%{
  private static class Token implements MarkdownTokenTypes {}

  private int yycolumn = 0;

  private Stack<Integer> stateStack = new Stack<Integer>();

  char parseDelimitedExitChar = 0;
  IElementType parseDelimitedReturnType = null;

  boolean isHeader = false;

  int currentIndent = 0;

  private String getTagNameFromTagEnd() {
    int until = 1;

    char c = 0;
    do {
      until++;
      c = yycharat(until);
    } while (Character.isLetter(c) || Character.isDigit(c));

    return yytext().toString().substring(2, until);
  }

  private static IElementType getDelimiterTokenType(char c) {
    switch (c) {
      case '"': return Token.DOUBLE_QUOTE;
      case '\'': return Token.SINGLE_QUOTE;
      case ')': return Token.RPAREN;
      case ']': return Token.RBRACKET;
      default: return Token.BAD_CHARACTER;
    }
  }

  private IElementType parseDelimited(IElementType contentsType) {
    char first = yycharat(0);
    char last = yycharat(yylength() - 1);

    stateStack.push(yystate());

    parseDelimitedExitChar = last;
    parseDelimitedReturnType = contentsType;

    yybegin(PARSE_DELIMITED);

    yypushback(yylength() - 1);
    return getDelimiterTokenType(first);
  }

  private void increaseIndent() {
    currentIndent = Math.max(currentIndent, (yycolumn & 0xffffc) + 4);
  }

  private void recalcIndent() {
    int newIndent = yylength() - 1;
    if (newIndent < currentIndent) {
      currentIndent = newIndent & 0xfffc;
    }
  }

  private void processEol() {
    int newlinePos = 1;
    while (newlinePos < yylength() && yycharat(newlinePos) != '\n') {
      newlinePos++;
    }

    // there is always one at 0 so that means there are two at least
    if (newlinePos != yylength()) {
      yypushback(yylength() - newlinePos);
      return;
    }

    recalcIndent();
    yybegin(YYINITIAL);
    yypushback(yylength() - 1);

    isHeader = false;
  }

  private void resetState() {
    yypushback(yylength());
    yybegin(AFTER_LINE_START);
  }

  private boolean isBlockTag(String tagName) {
    return true;
  }

  private void processTagStart() {
  }

  private boolean processTagEnd(String tagName) {
    return true;
  }

%}

DIGIT = [0-9]
ALPHANUM = [a-zA-Z0-9]
WHITE_SPACE = [ \t\f]
EOL = "\n"

DOUBLE_QUOTED_TEXT = \" (\\\" | [^\n\"])* \"
SINGLE_QUOTED_TEXT = "'" (\\"'" | [^\n'])* "'"
QUOTED_TEXT = {SINGLE_QUOTED_TEXT} | {DOUBLE_QUOTED_TEXT}

URL = [^ \f\n\t<>]+

TAG_START = "<" {ALPHANUM}+
TAG_END = "</" {ALPHANUM}+ {WHITE_SPACE}* ">"

LINK_ID = ({ALPHANUM} | {WHITE_SPACE})*

%state HTML_BLOCK, TAG_START, AFTER_LINE_START, LINK, LINK_DEF, PARSE_DELIMITED, CODE

%%



<YYINITIAL> {
  {TAG_START} {
    String tagName = yytext().toString().substring(1);
    if (isBlockTag(tagName)) {
      processTagStart();
      yybegin(HTML_BLOCK);
    } else {
      yybegin(TAG_START);
    }
    return Token.TAG_NAME;
  }

  {WHITE_SPACE}+ {
    if (yycolumn + yylength() >= currentIndent + 4) {
      yybegin(CODE);
    }
    return Token.WHITE_SPACE;
  }

  // Setext headers
  "="+ |
  "-"+ {
    return yycharat(0) == '=' ? Token.SETEXT_1 : Token.SETEXT_2;
  }

  // atx headers
  "#"+ {
    isHeader = true;
    return Token.ATX_HEADER;
  }

  // blockquote
  ">" {
    return Token.BLOCK_QUOTE;
  }

  // Unordered lists
  [*+-] / {WHITE_SPACE} {
    increaseIndent();
    return Token.LIST_BULLET;
  }

  // Ordered lists
  {DIGIT}+ "." {
    increaseIndent();
    return Token.LIST_NUMBER;
  }

  // Horizontal rule
  {WHITE_SPACE}* ("*" ({WHITE_SPACE}* "*"){2,10} | \
                  "-" ({WHITE_SPACE}* "-"){2,10} | \
                  "_"{3,10} \
                 ) {WHITE_SPACE}* {
    return Token.HORIZONTAL_RULE;
  }

  {EOL} | . {
    resetState();
  }
}

<AFTER_LINE_START> {

  // Escaping
  \\[\\`*_{}\[\]()#+-.!] {
    return Token.TEXT;
  }

  // atx header end
  "#"+ / {EOL} {
    if (isHeader) {
      return Token.ATX_HEADER;
    }
    return Token.TEXT;
  }

  {TAG_START} {
    yybegin(TAG_START);
    return Token.TAG_NAME;
  }

  {WHITE_SPACE}+ {
    return Token.TEXT;
  }

  // Links
  "!"? "[" [^\n\[]* "]" / {WHITE_SPACE}* ( \
    "(" {WHITE_SPACE}* {URL} {WHITE_SPACE}* ({QUOTED_TEXT} {WHITE_SPACE}*)? ")" | \
    "[" {LINK_ID} "]" \
  ) {
    if (yycharat(0) == '!') {
      yypushback(1);
      return Token.EXCLAMATION_MARK;
    }

    yybegin(LINK);
    return parseDelimited(Token.TEXT);
  }

  "[" {LINK_ID} "]" / ":" {
    yybegin(LINK_DEF);
    return parseDelimited(Token.LINK_ID);
  }

  // Emphasis
  {WHITE_SPACE}+ ("*" | "_") {WHITE_SPACE}+ {
    return Token.TEXT;
  }

  "*" | "_" {
    return Token.EMPH;
  }
  "**" | "__" {
    return Token.STRONG;
  }

  // Backticks (code)
  "`"+ {
    return Token.BACKTICK;
  }

  ({EOL} {WHITE_SPACE}*)+ {
    processEol();
    return Token.EOL;
  }

  // optimize
  {ALPHANUM}+ ({WHITE_SPACE}+ {ALPHANUM}+)* {
    return Token.TEXT;
  }

  . { return Token.TEXT; }

}

<LINK> {
  {WHITE_SPACE}+ {
    return Token.WHITE_SPACE;
  }

  "[" {LINK_ID} "]" {
    yybegin(AFTER_LINE_START);
    return parseDelimited(Token.LINK_ID);
  }

  "(" { return Token.LPAREN; }
  ")" {
    yybegin(AFTER_LINE_START);
    return Token.RPAREN;
  }

  {URL} { return Token.URL; }

  {QUOTED_TEXT} { return parseDelimited(Token.TEXT); }

  {EOL} | . { resetState(); }
}

<LINK_DEF> {
  {WHITE_SPACE}+ { return Token.WHITE_SPACE; }

  ":" { return Token.COLON; }

  "<" { return Token.LT; }
  ">" { return Token.GT; }
  {URL} { return Token.URL; }

  {QUOTED_TEXT} | "(" (\\")" | [^\n)])* ")" {
    return parseDelimited(Token.TEXT);
  }

  {EOL} | . { resetState(); }
}

<PARSE_DELIMITED> {
  {EOL} { resetState(); }

  [^\n]. {
    if (yycharat(0) == '\\') {
      return parseDelimitedReturnType;
    }
    yypushback(1);
    if (yycharat(0) == parseDelimitedExitChar) {
      yybegin(stateStack.pop());
      return getDelimiterTokenType(yycharat(0));
    }
    return parseDelimitedReturnType;
  }

  {EOL} | . {
    if (yycharat(0) == parseDelimitedExitChar) {
      yybegin(stateStack.pop());
      return getDelimiterTokenType(yycharat(0));
    }
    return parseDelimitedReturnType;
  }
}

<HTML_BLOCK> {
  {TAG_START} {
    processTagStart();
    return Token.HTML_BLOCK;
  }

  {TAG_END} {
    String tagName = getTagNameFromTagEnd();
    if (processTagEnd(tagName)) {
      yybegin(YYINITIAL);
    }
    return Token.HTML_BLOCK;
  }

  {EOL} | . { return Token.HTML_BLOCK; }
}

<CODE> {
  ({EOL} {WHITE_SPACE}*)+ {
    processEol();
    return Token.EOL;
  }

  {EOL} | . { return Token.CODE; }
}

. { return Token.BAD_CHARACTER; }