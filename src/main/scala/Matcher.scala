package mdlink

import cats.implicits.*
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
  case Many(matcher: Seq[String])
  case Regexp(matcher: Regex)

object Matcher:
  given Encoder[Matcher] = Encoder.instance {
    case Regexp(m) => ???
    case Simple(m) => m.asJson
    case Many(m)   => m.asJson
  }

  given Decoder[Matcher] = Decoder.instance { cursor =>
    val simple = cursor.as[String].map(Simple.apply)
    val many = cursor.as[Seq[String]].map(Many.apply)
    List(simple, many).reduceLeft(_ orElse _)
  }

  given Eq[Matcher] = Eq.fromUniversalEquals
  given Arbitrary[Matcher] = Arbitrary {
    val simple = Gen.alphaStr map Simple.apply
    val many = Gen.listOf(Gen.alphaStr) map Many.apply
    Gen.oneOf(simple, many)
  }
