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

case class Selector private (source: String, private val evaluator: Evaluator)

object Selector:
  def apply(query: String): Either[Throwable, Selector] =
    safeEither(unsafe(query))

  private[mdlink] given Conversion[Selector, Evaluator] = _.evaluator

  private[mdlink] def unsafe(query: String): Selector =
    Selector(query, QueryParser.parse(query))

  given Eq[Selector] = Eq.instance { (a, b) => a.source == b.source }

  given Decoder[Selector] = Decoder[String] flatMap { s =>
    Selector(s) match
      case Left(e)         => Decoder.failedWithMessage(e.getMessage)
      case Right(selector) => Decoder.const(selector)
  }

  given Encoder[Selector] = Encoder.instance(_.source.asJson)

  given Arbitrary[Selector] = Arbitrary {
    genLowerNonEmpty.map { nonEmpty =>
      Selector(nonEmpty) match
        case Left(e)         => throw e
        case Right(selector) => selector
    }
  }
