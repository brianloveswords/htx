package dev.bjb.htx

import cats.Eq
import cats.implicits.*
import org.http4s.Uri
import org.http4s.implicits.*

import scala.util.control.NoStackTrace

// htx example.com "[{h1.title}]({@})"
// htx example.com author:"meta[property='twitter:title']" "[{h1.title}]({@})"

enum ReplacerEntry:
  case AutoUri(uri: Uri)
  case Standard(extractor: Extractor)

import ReplacerEntry.*
given Eq[ReplacerEntry] = Eq.instance {
  case (Standard(a), Standard(b)) => a === b
  case (AutoUri(a), AutoUri(b))   => a === b
  case _                          => false
}

type ExtractorTemplateResult = Either[ExtractorTemplateError, ExtractorTemplate]

case class ExtractorTemplate private (
    replacements: Map[String, ReplacerEntry],
    template: Template,
    uri: Option[Uri],
)
case object ExtractorTemplate:
  import ExtractorTemplateError.*

  given Eq[ExtractorTemplate] = Eq.instance { (a, b) =>
    a.replacements === b.replacements &&
    a.template === b.template &&
    a.uri === b.uri
  }

  lazy val extractRe = raw"\{(.*?)\}".r
  def from(
      extractors: ExtractorMap,
      template: Template,
      uri: Option[Uri],
  ): Either[ExtractorTemplateError, ExtractorTemplate] = for
    tpl <- Right(template.value)
    matches = extractRe.findAllMatchIn(tpl).map(m => m.group(1)).toList
    ex <-
      if matches.lengthIs == 0 then Left(NoReplacements(template))
      else mergeMatches(extractors, template, matches, uri)
  yield ExtractorTemplate(ex, template, uri)

  def unsafe(
      replacements: Map[String, ReplacerEntry],
      template: Template,
      uri: Option[Uri],
  ): ExtractorTemplate =
    new ExtractorTemplate(replacements, template, uri)

  lazy val emptyReplacementMap =
    Map.empty[String, ReplacerEntry].asRight[ExtractorTemplateError]

  private def mergeMatches(
      extractors: ExtractorMap,
      template: Template,
      matches: List[String],
      uri: Option[Uri],
  ): Either[ExtractorTemplateError, Map[String, ReplacerEntry]] = for
    ex <- Right(extractors)
    keys = extractors.keySet
    unused = keys.diff(matches.toSet)
    replacements <-
      if unused.sizeIs == 0 then Right(ex.map((k, v) => k -> Standard(v)))
      else Left(UnusedExtracts(ex.view.filterKeys(unused.contains(_)).toMap))
    auto <- matches.foldLeft(emptyReplacementMap) { (acc, m) =>
      acc.flatMap { acc =>
        m match
          case "@" =>
            uri match
              case Some(u) => Right(acc + ("@" -> AutoUri(u)))
              case None    => Left(UnfulfilledAutoUri(template))
          case m =>
            Selector(m) match
              case Left(err) =>
                Left(InvalidSelectorFromTemplate(template, m, err.getMessage))
              case Right(sel) =>
                Right(acc + (m -> Standard(Extractor(sel))))
      }
    }
  yield auto ++ replacements
