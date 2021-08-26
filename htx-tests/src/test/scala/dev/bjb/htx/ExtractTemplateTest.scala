package dev.bjb.htx

import scala.util.control.NoStackTrace

// htx example.com "[{h1.title}]({@})"
// htx example.com author:"meta[property='twitter:title']" "[{h1.title}]({@})"

enum ExtractTemplateError extends NoStackTrace:
  case NoReplacements(template: Template)
  case UnusedExtracts(extractors: Map[String, Extract])

case class ExtractTemplate private (
    extractorMap: Map[String, Extract],
    template: Template,
)
case object ExtractTemplate:
  import ExtractTemplateError.*

  lazy val extractRe = raw"\{(.*?)\}".r
  def from(
      extractors: Map[String, Extract],
      template: Template,
  ): Either[ExtractTemplateError, ExtractTemplate] = for
    tpl <- Right(template.value)
    matches = extractRe.findAllMatchIn(tpl).map(m => m.group(1)).toList
    ex <-
      if matches.lengthIs == 0 then Left(NoReplacements(template))
      else mergeMatches(matches, extractors)
  yield ExtractTemplate(ex, template)

  private def mergeMatches(
      matches: List[String],
      extractors: Map[String, Extract],
  ): Either[ExtractTemplateError, Map[String, Extract]] = for
    ex <- Right(extractors)
    keys = extractors.keySet
    unused = keys.diff(matches.toSet)
    _ <-
      if unused.sizeIs == 0 then Right(extractors)
      else Left(UnusedExtracts(ex.view.filterKeys(unused.contains(_)).toMap))
  yield ???

class ExtractTemplateTest extends CommonSuite:
  import ExtractTemplateError.*

  test("no replacements") {
    val template = Template("cool")
    val result = ExtractTemplate.from(
      extractors = Map(),
      template = template,
    )
    assertEquals(result, Left(NoReplacements(template)))
  }

  test("unused extracts") {
    val template = Template("x{author}x")
    val title = ("title" -> Extract(Selector.unsafe("h1.title")))
    val author = ("author" -> Extract(
      Selector.unsafe("meta[name='author']"),
      Some("content"),
      Some("Unknown Author"),
    ))
    val result = ExtractTemplate.from(
      extractors = Map(title, author),
      template = template,
    )
    assertEquals(result, Left(UnusedExtracts(Map(title))))
  }
