package mdlink

import cats.Eq
import cats.implicits.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.scalacheck.Arbitrary
import org.scalacheck.Gen

import scala.util.matching.Regex

enum Matcher:
  case Single(m: String)
  case Many(m: Seq[String])
  case Pattern(m: Regex)

private case class PatternSerializable(
    pattern: String,
    `type`: String = "regex",
):
  def toPattern: Matcher = Matcher.Pattern(pattern.r)
private object PatternSerializable:
  def apply(pattern: Matcher.Pattern): PatternSerializable =
    PatternSerializable(pattern.m.toString)

object Matcher:
  given Encoder[Matcher] = Encoder.instance {
    case p @ Pattern(_) => PatternSerializable(p).asJson
    case Single(m)      => m.asJson
    case Many(m)        => m.asJson
  }

  given Decoder[Matcher] = Decoder.instance { cursor =>
    val simple = cursor.as[String].map(Single.apply)
    val many = cursor.as[Seq[String]].map(Many.apply)
    val pattern = cursor.as[PatternSerializable].map(_.toPattern)
    List(simple, many, pattern).reduceLeft(_ orElse _)
  }

  given Eq[Matcher] = Eq.instance {
    case (Pattern(p1), Pattern(p2)) => p1.toString === p2.toString
    case (m1, m2)                   => m1 == m2
  }

  given Arbitrary[Matcher] = Arbitrary {
    val simple = Gen.alphaStr map Single.apply
    val many = Gen.listOf(Gen.alphaStr) map Many.apply
    val pattern = for
      str <- Gen.alphaStr
      re = str.r
    yield Pattern(re)
    Gen.oneOf(simple, many, pattern)
  }
