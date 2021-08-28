package dev.bjb.htx

import cats.effect.IO
import cats.implicits.*
import dev.bjb.htx.grammar.TemplateBaseVisitor
import dev.bjb.htx.grammar.TemplateLexer
import dev.bjb.htx.grammar.TemplateParser
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.*

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.*
import scala.jdk.CollectionConverters.*
import cats.effect.Async
import cats.effect.Concurrent
import cats.Parallel
import scala.util.control.NoStackTrace
import scala.util.control.NonFatal

enum Part:
  case Text(inner: String)
  case Pattern(inner: String)
export Part.*

case class TemplateEvaluator(parts: Seq[Part]):
  val patterns = parts.collect { case Pattern(p) => p }.toSet

  def eval[F[_]: Async: Concurrent: Parallel](
      ctx: Map[String, List[String]],
  ): F[List[String]] =
    type Entry = (Int, List[List[String]])
    def process(s: String) = Async[F].pure(s)
    val empty: F[Entry] = Async[F].pure((1, List.empty))
    parts.foldLeft(empty) { (pair, part) =>
      pair.flatMap { (cardinality, acc) =>
        part match
          case Text(s) => Async[F].pure((cardinality, acc :+ List(s)))
          case Pattern(p) =>
            val notFound: F[Entry] =
              Async[F].pure((cardinality, List(List(s"<missing: $p>"))))

            // TODO: cache this computation
            val computed: F[Entry] =
              ctx.get(p).fold(notFound) { values =>
                values.map(process).parSequence map { rendered =>
                  (cardinality * values.length, acc :+ rendered)
                }
              }
            computed
      }
    } map { (cardinality, lists) =>
      val infiniteLists = lists.map { list =>
        val arr = list.toArray
        val len = arr.size
        new Iterator[String] {
          var idx = 0
          def hasNext = true
          def next =
            val result = arr(idx)
            idx = if idx + 1 >= len then 0 else idx + 1
            result
        }
      }

      (1 to cardinality).toList.map { i =>
        infiniteLists.foldLeft("") { (acc, list) =>
          acc ++ list.next()
        }
      }
    }

object TemplateEvaluator:
  def apply(input: String): Either[TemplateParseError, TemplateEvaluator] = try
    val visitor = TemplateVisitor()
    val parser = createParser(input)
    val parts = visitor.visit(parser.template())
    Right(TemplateEvaluator(parts))
  catch { case err: TemplateParseError => Left(err) }

  def unsafe(input: String): TemplateEvaluator =
    apply(input).fold(err => throw err, identity)

  def createParser(input: String): TemplateParser =
    val parser = TemplateParser(
      CommonTokenStream(
        TemplateLexer(
          CharStreams.fromString(input),
        ),
      ),
    )
    parser.removeErrorListeners
    parser.addErrorListener(TemplateErrorListener())
    parser

sealed trait TemplateParseError extends NoStackTrace
case object MissingInput extends TemplateParseError:
  override def getMessage: String = s"cannot parse empty input"
case class ParseInputError(
    line: Int,
    charPositionInLine: Int,
    msg: String,
) extends TemplateParseError:
  override def getMessage: String = s"$line:$charPositionInLine: $msg"

private class TemplateErrorListener extends BaseErrorListener:
  override def syntaxError(
      recognizer: Recognizer[?, ?],
      offendingSymbol: Any,
      line: Int,
      charPositionInLine: Int,
      msg: String,
      e: RecognitionException,
  ): Unit =
    throw new ParseInputError(line, charPositionInLine, msg)

private class TemplateVisitor extends TemplateBaseVisitor[Seq[Part]]:
  import dev.bjb.htx.grammar.TemplateParser.*

  val empty: Seq[Part] = Seq.empty
  def descend(ctx: ParserRuleContext): Seq[Part] =
    ctx.children.asScala.toSeq.foldLeft(empty) { (acc, child) =>
      acc ++ visit(child)
    }

  def unescape(s: String): String = s
    .replace("\\{", "{")
    .replace("\\}", "}")
    .replace("\\n", "\n")
    .replace("\\t", "\t")

  override def visitTemplate(ctx: TemplateContext) =
    descend(ctx)

  override def visitPart(ctx: PartContext) =
    descend(ctx)

  override def visitPattern(ctx: PatternContext) =
    val inner = ctx.getText().tail.init.trim
    Seq(Pattern(inner))

  override def visitText(ctx: TextContext) =
    Seq(Text(unescape(ctx.getText())))

private def debugCtx(typ: String, ctx: ParserRuleContext): Unit =
  val t = ctx.getText
  println(s"visiting $typ with this context $t")
