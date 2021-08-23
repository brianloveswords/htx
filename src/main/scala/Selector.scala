package mdlink

import cats.implicits.*
import cats.Eq
import io.circe.Encoder
import io.circe.Decoder
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.jsoup.Jsoup
import org.jsoup.select.QueryParser
import org.jsoup.select.Evaluator
import scala.jdk.CollectionConverters.*
import org.scalacheck.Arbitrary

case class Selector private (source: String, evaluator: Evaluator):
  private[mdlink] def toEvaluator: Evaluator = evaluator

object Selector:
  def apply(query: String): Either[Throwable, Selector] = safeEither(
    QueryParser.parse(query),
  ) map (Selector(query, _))

  given Eq[Selector] = Eq.instance { (a, b) => a.source == b.source }

  given Decoder[Selector] = Decoder[SelectorSerializable] flatMap { s =>
    s.toSelector match
      case Left(e)         => Decoder.failedWithMessage(e.getMessage)
      case Right(selector) => Decoder.const(selector)
  }

  given Encoder[Selector] = Encoder.instance {
    case Selector(source, evaluator) =>
      SelectorSerializable(source).asJson
  }

  given Arbitrary[Selector] = Arbitrary {
    genLowerNonEmpty.map { nonEmpty =>
      Selector(nonEmpty) match
        case Left(e)         => throw e
        case Right(selector) => selector
    }
  }

private case class SelectorSerializable(`source`: String):
  def toSelector: Either[Throwable, Selector] = Selector(`source`)

private object SelectorSerializable:
  def apply(selector: Selector): SelectorSerializable =
    SelectorSerializable(selector.source)
