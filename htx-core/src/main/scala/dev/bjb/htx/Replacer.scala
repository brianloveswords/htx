package dev.bjb.htx

import cats.Eq
import cats.implicits.*
import org.http4s.Uri
import org.http4s.implicits.*

import scala.util.control.NoStackTrace
import scala.util.matching.Regex.Match

// htx example.com "[{h1.title}]({@})"
// htx example.com author:"meta[property='twitter:title']" "[{h1.title}]({@})"

case class Offset(start: Int, end: Int)
given Eq[Offset] = Eq.fromUniversalEquals

enum ReplaceUnit(offset: Offset):
  case AutoUri(offset: Offset, uri: Uri) extends ReplaceUnit(offset)
  case Standard(offset: Offset, extractor: Extractor)
      extends ReplaceUnit(offset)

import ReplaceUnit.*
given Eq[ReplaceUnit] = Eq.instance {
  case (Standard(a1, a2), Standard(b1, b2)) => (a1, a2) === (b1, b2)
  case (AutoUri(a1, a2), AutoUri(b1, b2))   => (a1, a2) === (b1, b2)
  case _                                    => false
}

extension (pair: (String, Extractor))
  def toReplaceMapEntry(offset: Offset): (String, ReplaceUnit) =
    (pair._1, Standard(offset, pair._2))

type ReplaceMap = Map[String, ReplaceUnit]

type ReplacerResult = Either[ReplacerError, Replacer]

case class Replacer private (
    replacements: ReplaceMap,
    template: Template,
    uri: Option[Uri],
):
  def apply(soup: PureSoup): Option[String] =
    ???

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
    matches = extractRe.findAllMatchIn(tpl).toList
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
      matches: List[Match],
      uri: Option[Uri],
  ): Either[ReplacerError, ReplaceMap] = for
    ex <- Right(extractors)
    keys = extractors.keySet
    unused = keys.diff(matches.toSet.map(_.group(1)))
    _ <-
      if unused.sizeIs == 0 then Right(())
      else Left(UnusedExtracts(ex.view.filterKeys(unused.contains(_)).toMap))
    result <- matches.foldLeft(emptyReplacementMap) { (acc, m) =>
      acc.flatMap { acc =>
        val text = m.group(1)
        val offset = Offset(m.start, m.end)

        lazy val autoUriError = Left(UnfulfilledAutoUri(template))

        inline def success(
            key: String,
            value: ReplaceUnit,
        ): Either[ReplacerError, ReplaceMap] =
          Right(acc + (key -> value))

        def invalidSelectorError(e: Throwable) =
          Left(InvalidSelectorFromTemplate(template, text, e.getMessage))

        text match
          case "@" =>
            uri.fold(autoUriError)(u => success(text, AutoUri(offset, u)))
          case m =>
            Selector(m).fold(
              invalidSelectorError,
              sel => success(m, Standard(offset, Extractor(sel))),
            )

      }
    }
  yield result
