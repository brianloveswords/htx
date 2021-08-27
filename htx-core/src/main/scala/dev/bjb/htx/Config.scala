package dev.bjb.htx

import cats.Eq
import cats.implicits.*
import io.circe.generic.auto.*
import org.scalacheck.Arbitrary
import org.scalacheck.Gen

type Config = Seq[ConfigEntry]
type ExtractorMap = Map[String, Extractor]

case class ConfigEntry(
    url: Matcher,
    extractors: ExtractorMap,
    template: Template,
)

object ConfigEntry:
  import Arbitrary.arbitrary

  given Eq[ConfigEntry] = Eq.instance { (a, b) =>
    val ConfigEntry(m1, e1, t1) = a
    val ConfigEntry(m2, e2, t2) = b
    (m1, e1, t1) === (m2, e2, t2)
  }

  given Arbitrary[ExtractorMap] = Arbitrary {
    Gen.mapOf(Gen.zip(Gen.alphaStr, arbitrary[Extractor]))
  }

  given Arbitrary[ConfigEntry] = Arbitrary {
    for
      matcher <- arbitrary[Matcher]
      extractors <- arbitrary[ExtractorMap]
      template <- arbitrary[Template]
    yield ConfigEntry(matcher, extractors, template)
  }
