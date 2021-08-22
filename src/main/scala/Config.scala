package mdlink

import cats.Eq
import cats.syntax.*
import cats.effect.IO
import scala.util.matching.Regex
import io.circe.generic.auto.*
import io.circe.syntax.*
import io.circe.parser.*
import io.circe.*
import org.scalacheck.Arbitrary
import org.scalacheck.Gen

type Config = Seq[ConfigEntry]

case class ConfigEntry(
    matcher: Matcher,
    extractors: ExtractorMap,
    template: Template,
)

type ExtractorMap = Map[String, Extractor]

case class Extractor(
    selector: String,
    fallback: Option[String],
)

enum Matcher:
  case Simple(matcher: String)
  case List(matcher: Seq[String])
  case Regexp(matcher: Regex)

case class Template(value: String)
object Template:
  given Encoder[Template] = Encoder.instance { _.value.asJson }
  given Decoder[Template] = Decoder.instance { cursor =>
    cursor.as[String].map(Template.apply)
  }

  given Eq[Template] = Eq.fromUniversalEquals
  given Arbitrary[Template] = Arbitrary {
    Gen.listOf(Gen.alphaChar) map { chars =>
      Template(chars.mkString)
    }
  }
