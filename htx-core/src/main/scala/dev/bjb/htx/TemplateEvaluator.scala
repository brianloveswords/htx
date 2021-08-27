package dev.bjb.htx

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.*
import dev.bjb.htx.grammar.TemplateParser
import dev.bjb.htx.grammar.TemplateLexer
import dev.bjb.htx.grammar.TemplateBaseVisitor

enum Part:
  case Text(inner: String)
  case Pattern(inner: String)
export Part.*

case class TemplateEvaluator(parts: Seq[Part]):
  def eval = ???

object TemplateEvaluator:
  def apply(input: String): TemplateEvaluator =
    val visitor = TemplateVisitor()
    val parser = TemplateParser(
      CommonTokenStream(
        TemplateLexer(
          CharStreams.fromString(input),
        ),
      ),
    )
    val parts = visitor.visit(parser.template())
    TemplateEvaluator(parts)

private class TemplateVisitor extends TemplateBaseVisitor[Seq[Part]]:
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

def debugCtx(typ: String, ctx: ParserRuleContext): Unit =
  val t = ctx.getText
  println(s"visiting $typ with this context $t")
