package mdlink

import cats.implicits.*
import org.jsoup.Jsoup
import org.jsoup.select.QueryParser
import org.jsoup.select.Evaluator
import scala.jdk.CollectionConverters.*

case class Selector(source: String, evaluator: Evaluator):
  def toEvaluator: Evaluator = evaluator

object Selector:
  def apply(query: String): Either[Throwable, Selector] = safeEither(
    QueryParser.parse(query),
  ) map (Selector(query, _))

  private case class SelectorSerializable(`source`: String):
    def toSelector: Either[Throwable, Selector] = Selector(`source`)

  private object SelectorSerializable:
    def apply(selector: Selector): SelectorSerializable =
      SelectorSerializable(selector.source)
