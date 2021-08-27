package dev.bjb.htx

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.*
import dev.bjb.htx.grammar as g

enum Part:
  case Text(inner: String)
  case Pattern(inner: String)
import Part.*

object TemplateParser:
  def apply(input: String): g.TemplateParser = new g.TemplateParser(
    CommonTokenStream(
      g.TemplateLexer(
        CharStreams.fromString(input),
      ),
    ),
  )

class TemplateParserTest extends CommonSuite:
  test("1 part, all text") {
    val visitor = TemplateVisitor()
    val parser = TemplateParser("oh hello")
    val tree = parser.template()
    val result = visitor.visit(tree)
    assertEquals(result, Seq(Text("oh hello")))
  }

def debugCtx(typ: String, ctx: ParserRuleContext): Unit =
  val t = ctx.getText
  println(s"visiting $typ with this context $t")

class TemplateVisitor extends g.TemplateBaseVisitor[Seq[Part]]:
  import dev.bjb.htx.grammar.TemplateParser.*

  var parts: Seq[Part] = Vector.empty

  override def visitTemplate(ctx: TemplateContext) =
    visitChildren(ctx)

  override def visitPart(ctx: PartContext) =
    parts ++ visitChildren(ctx)

  override def visitPattern(ctx: PatternContext) =
    // drop the { and }
    val inner = ctx.getText().tail.init
    parts :+ Pattern(inner)

  override def visitText(ctx: TextContext) =
    parts :+ Text(ctx.getText())
