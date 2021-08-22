package mdlink

import cats.Eq
import cats.effect.IO
import cats.syntax.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import org.scalacheck.Arbitrary
import org.scalacheck.Gen

import scala.util.matching.Regex

enum Matcher:
  case Simple(matcher: String)
  case List(matcher: Seq[String])
  case Regexp(matcher: Regex)

object Matcher:
  given Encoder[Matcher] = Encoder.instance {
    case Simple(m) => m.asJson
    case List(m)   => ???
    case Regexp(m) => ???
  }

  given Decoder[Matcher] = Decoder.instance { cursor =>
    cursor.as[String].map(Simple.apply)
  }

  given Eq[Matcher] = Eq.fromUniversalEquals
  given Arbitrary[Matcher] = Arbitrary {
    Gen.const(Simple("1"))
  }
