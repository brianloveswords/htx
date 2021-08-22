package mdlink

import cats.Eq
import cats.syntax.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import org.scalacheck.Arbitrary
import org.scalacheck.Gen

import scala.util.matching.Regex

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
