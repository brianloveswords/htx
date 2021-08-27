package dev.bjb.htx

import dev.bjb.htx.grammar.TemplateLexer
import dev.bjb.htx.grammar.TemplateParser
import dev.bjb.htx.grammar.TemplateBaseVisitor
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.*

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
      "1+1\n",
    )
    val tree = parser.top()

    visitor.visit(tree)
    visitor.total
    assertEquals(visitor.total, 2) //
  }

class TemplateVisitor extends TemplateBaseVisitor[Int]:
  import TemplateParser.*

  var total: Int = 0

  override def visitTop(ctx: TopContext): Int =
    total += 1
    total
