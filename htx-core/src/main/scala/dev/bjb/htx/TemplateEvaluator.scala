package dev.bjb.htx

import cats.implicits.*
import dev.bjb.htx.grammar.TemplateBaseVisitor
import dev.bjb.htx.grammar.TemplateLexer
import dev.bjb.htx.grammar.TemplateParser
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.*

import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters.*

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
    TemplateEvaluator(parts.toSeq)

private class TemplateVisitor extends TemplateBaseVisitor[Seq[Part]]:
  import dev.bjb.htx.grammar.TemplateParser.*

  val empty: Seq[Part] = Seq.empty
  def descend(ctx: ParserRuleContext): Seq[Part] =
    ctx.children.asScala.toSeq.foldLeft(empty) { (acc, child) =>
      acc ++ visit(child)
    }

  def unescape(s: String): String =
    s.replace("\\{", "{").replace("\\}", "}")

  override def visitTemplate(ctx: TemplateContext) =
    descend(ctx)

  override def visitPart(ctx: PartContext) =
    descend(ctx)

  override def visitPattern(ctx: PatternContext) =
    // drop the { and }
    val inner = ctx.getText().tail.init
    Seq(Pattern(inner))

  override def visitText(ctx: TextContext) =
    Seq(Text(unescape(ctx.getText())))

def pairs(s: String): Iterator[String] =
  s.sliding(2) ++ List(s.last.toString)

private def debugCtx(typ: String, ctx: ParserRuleContext): Unit =
  val t = ctx.getText
  println(s"visiting $typ with this context $t")
