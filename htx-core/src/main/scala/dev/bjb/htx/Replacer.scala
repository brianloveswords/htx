package dev.bjb.htx

import cats.Eq
import cats.implicits.*
import org.http4s.Uri
import org.http4s.implicits.*

import scala.util.control.NoStackTrace

// htx example.com "[{h1.title}]({@})"
// htx example.com author:"meta[property='twitter:title']" "[{h1.title}]({@})"

enum ReplaceUnit:
  case AutoUri(uri: Uri)
  case Standard(extractor: Extractor)

import ReplaceUnit.*
given Eq[ReplaceUnit] = Eq.instance {
  case (Standard(a), Standard(b)) => a === b
  case (AutoUri(a), AutoUri(b))   => a === b
  case _                          => false
}

type ReplaceMap = Map[String, ReplaceUnit]

type ReplacerResult = Either[ReplacerError, Replacer]

case class Replacer private (
    replacements: ReplaceMap,
    template: Template,
    uri: Option[Uri],
)
case object Replacer:
  import ReplacerError.*

  given Eq[Replacer] = Eq.instance { (a, b) =>
    a.replacements === b.replacements &&
    a.template === b.template &&
    a.uri === b.uri
  }

  lazy val extractRe = raw"\{(.*?)\}".r
  def from(
      extractors: ExtractorMap,
      template: Template,
      uri: Option[Uri],
  ): Either[ReplacerError, Replacer] = for
    tpl <- Right(template.value)
    matches = extractRe.findAllMatchIn(tpl).map(m => m.group(1)).toList
    ex <-
      if matches.lengthIs == 0 then Left(NoReplacements(template))
      else mergeMatches(extractors, template, matches, uri)
  yield Replacer(ex, template, uri)

  def unsafe(
      replacements: ReplaceMap,
      template: Template,
      uri: Option[Uri],
  ): Replacer =
    new Replacer(replacements, template, uri)

  lazy val emptyReplacementMap =
    Map.empty[String, ReplaceUnit].asRight[ReplacerError]

  private def mergeMatches(
      extractors: ExtractorMap,
      template: Template,
      matches: List[String],
      uri: Option[Uri],
  ): Either[ReplacerError, ReplaceMap] = for
    ex <- Right(extractors)
    keys = extractors.keySet
    unused = keys.diff(matches.toSet)
    replacements <-
      if unused.sizeIs == 0 then Right(ex.map((k, v) => k -> Standard(v)))
      else Left(UnusedExtracts(ex.view.filterKeys(unused.contains(_)).toMap))
    auto <- matches.foldLeft(emptyReplacementMap) { (acc, m) =>
      acc.flatMap { acc =>
        lazy val autoUriError = Left(UnfulfilledAutoUri(template))

        inline def success(
            key: String,
            value: ReplaceUnit,
        ): Either[ReplacerError, ReplaceMap] =
          Right(acc + (key -> value))

        def invalidSelectorError(e: Throwable) =
          Left(InvalidSelectorFromTemplate(template, m, e.getMessage))

        m match
          case "@" =>
            uri.fold(autoUriError)(u => success("@", AutoUri(u)))
          case m =>
            Selector(m).fold(
              invalidSelectorError,
              sel => success(m, Standard(Extractor(sel))),
            )

      }
    }
  yield auto ++ replacements
