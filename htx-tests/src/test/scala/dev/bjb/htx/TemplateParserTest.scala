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

enum Part:
  case Text(inner: String)
  case Curly(inner: String)
import Part.*

class TemplateParserTest extends CommonSuite:
  test("1 part, all text") {
    val visitor = TemplateVisitor()
    val parser = getParser(
      TemplateLexer(_),
      TemplateParser(_),
      "oh hello!",
    )
    val tree = parser.text()
    val result = visitor.visit(tree)
    assertEquals(result, Seq(Text("oh hello!")))
  }

class TemplateVisitor extends TemplateBaseVisitor[Seq[Part]]:
  import TemplateParser.*

  var parts: Seq[Part] = Vector.empty

  override def visitText(ctx: TextContext) =
    parts :+ Text(ctx.getText())
