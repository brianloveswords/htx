package main

import cats.Eq
import cats.implicits.*
import io.circe.generic.auto.*
import org.scalacheck.Arbitrary
import org.scalacheck.Gen

type ExtractMap = Map[String, Extract]
type Config = Seq[ConfigEntry]

case class ConfigEntry(
    url: Matcher,
    extract: ExtractMap,
    template: Template,
)

object ConfigEntry:
  import Arbitrary.arbitrary

  given Eq[ConfigEntry] = Eq.instance { (a, b) =>
    val ConfigEntry(m1, e1, t1) = a
    val ConfigEntry(m2, e2, t2) = b
    (m1, e1, t1) === (m2, e2, t2)
  }

  given Arbitrary[ExtractMap] = Arbitrary {
    Gen.mapOf(Gen.zip(Gen.alphaStr, arbitrary[Extract]))
  }

  given Arbitrary[ConfigEntry] = Arbitrary {
    for
      matcher <- arbitrary[Matcher]
      extractors <- arbitrary[ExtractMap]
      template <- arbitrary[Template]
    yield ConfigEntry(matcher, extractors, template)
  }
