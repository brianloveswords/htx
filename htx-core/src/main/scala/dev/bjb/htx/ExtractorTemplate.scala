package dev.bjb.htx

import cats.Eq
import cats.implicits.*
import scala.util.control.NoStackTrace

// htx example.com "[{h1.title}]({@})"
// htx example.com author:"meta[property='twitter:title']" "[{h1.title}]({@})"

enum ExtractorTemplateError extends NoStackTrace:
  case NoReplacements(template: Template)
  case UnusedExtracts(extractors: ExtractorMap)

object ExtractorTemplateError:
  given Eq[ExtractorTemplateError] =
    Eq.fromUniversalEquals[ExtractorTemplateError]

case class ExtractorTemplate private (
    extractors: ExtractorMap,
    template: Template,
)
case object ExtractorTemplate:
  import ExtractorTemplateError.*

  given Eq[ExtractorTemplate] = Eq.instance { (a, b) =>
    a.extractors === b.extractors &&
    a.template === b.template
  }

  lazy val extractRe = raw"\{(.*?)\}".r
  def from(
      extractors: ExtractorMap,
      template: Template,
  ): Either[ExtractorTemplateError, ExtractorTemplate] = for
    tpl <- Right(template.value)
    matches = extractRe.findAllMatchIn(tpl).map(m => m.group(1)).toList
    ex <-
      if matches.lengthIs == 0 then Left(NoReplacements(template))
      else mergeMatches(extractors, matches)
  yield ExtractorTemplate(ex, template)

  def unsafe(
      extractors: ExtractorMap,
      template: Template,
  ): ExtractorTemplate =
    new ExtractorTemplate(extractors, template)

  private def mergeMatches(
      extractors: ExtractorMap,
      matches: List[String],
  ): Either[ExtractorTemplateError, ExtractorMap] = for
    ex <- Right(extractors)
    keys = extractors.keySet
    unused = keys.diff(matches.toSet)
    _ <-
      if unused.sizeIs == 0 then Right(extractors)
      else Left(UnusedExtracts(ex.view.filterKeys(unused.contains(_)).toMap))
  yield matches.map(m => (m, Extractor.unsafe(m))).toMap ++ ex
