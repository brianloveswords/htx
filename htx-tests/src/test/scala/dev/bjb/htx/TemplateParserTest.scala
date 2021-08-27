package dev.bjb.htx

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.*
import dev.bjb.htx.grammar.TemplateLexer
import dev.bjb.htx.grammar.TemplateParser
import dev.bjb.htx.grammar.TemplateBaseVisitor

def getParser[L <: Lexer, P <: Parser](
    lexer: CharStream => L,
    parser: CommonTokenStream => P,
    contents: String,
): P = parser(CommonTokenStream(lexer(CharStreams.fromString(contents))))

class TemplateParserTest extends CommonSuite:
  test("ok".only) {
    val visitor = TemplateVisitor()
    val parser = getParser(
      TemplateLexer(_),
      TemplateParser(_),
      "oh hello!",
    )
    val tree = parser.text()
    val result = visitor.visit(tree)
    assertEquals(result, "oh hello!")
  }

class TemplateVisitor extends TemplateBaseVisitor[String]:
  import TemplateParser.*

  override def visitText(ctx: TextContext) =
    ctx.getText()
