package dev.bjb.htx

import cats.Eq
import cats.implicits.*
import scala.util.control.NoStackTrace
import org.http4s.Uri

// htx example.com "[{h1.title}]({@})"
// htx example.com author:"meta[property='twitter:title']" "[{h1.title}]({@})"

case class ExtractorTemplate private (
    extractors: ExtractorMap,
    template: Template,
    uri: Option[Uri],
)
case object ExtractorTemplate:
  import ExtractorTemplateError.*

  given Eq[ExtractorTemplate] = Eq.instance { (a, b) =>
    a.extractors === b.extractors &&
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
      else mergeMatches(extractors, template, matches)
  yield ExtractorTemplate(ex, template, uri)

  def unsafe(
      extractors: ExtractorMap,
      template: Template,
      uri: Option[Uri],
  ): ExtractorTemplate =
    new ExtractorTemplate(extractors, template, uri)

  private def mergeMatches(
      extractors: ExtractorMap,
      template: Template,
      matches: List[String],
  ): Either[ExtractorTemplateError, ExtractorMap] = for
    ex <- Right(extractors)
    keys = extractors.keySet
    unused = keys.diff(matches.toSet)
    _ <-
      if unused.sizeIs == 0 then Right(extractors)
      else Left(UnusedExtracts(ex.view.filterKeys(unused.contains(_)).toMap))
    empty = Map.empty[String, Extractor].asRight[ExtractorTemplateError]
    auto <- matches.foldLeft(empty) { (acc, m) =>
      acc.flatMap { acc =>
        Selector(m) match
          case Left(err) =>
            Left(InvalidSelectorFromTemplate(template, m, err.getMessage))
          case Right(sel) =>
            Right(acc + (m -> Extractor(sel)))
      }
    }
  yield matches.map(m => (m, Extractor.unsafe(m))).toMap ++ ex
