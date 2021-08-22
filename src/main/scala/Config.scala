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

type Config = Seq[ConfigEntry]

case class ConfigEntry(
    matcher: Matcher,
    extractors: ExtractorMap,
    template: Template,
)

type ExtractorMap = Map[String, Extractor]
