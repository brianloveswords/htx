package dev.bjb.htx

import cats.Eq
import cats.implicits.*
import scala.util.control.NoStackTrace

// htx example.com "[{h1.title}]({@})"
// htx example.com author:"meta[property='twitter:title']" "[{h1.title}]({@})"

enum ExtractTemplateError extends NoStackTrace:
  case NoReplacements(template: Template)
  case UnusedExtracts(extractors: Map[String, Extract])

object ExtractTemplateError:
  given Eq[ExtractTemplateError] = Eq.fromUniversalEquals[ExtractTemplateError]

case class ExtractTemplate private (
    extractors: Map[String, Extract],
    template: Template,
)
case object ExtractTemplate:
  import ExtractTemplateError.*

  given Eq[ExtractTemplate] = Eq.instance { (a, b) =>
    a.extractors === b.extractors &&
    a.template === b.template
  }

  lazy val extractRe = raw"\{(.*?)\}".r
  def from(
      extractors: Map[String, Extract],
      template: Template,
  ): Either[ExtractTemplateError, ExtractTemplate] = for
    tpl <- Right(template.value)
    matches = extractRe.findAllMatchIn(tpl).map(m => m.group(1)).toList
    ex <-
      if matches.lengthIs == 0 then Left(NoReplacements(template))
      else mergeMatches(extractors, matches)
  yield ExtractTemplate(ex, template)

  def unsafe(
      extractors: Map[String, Extract],
      template: Template,
  ): ExtractTemplate =
    new ExtractTemplate(extractors, template)

  private def mergeMatches(
      extractors: Map[String, Extract],
      matches: List[String],
  ): Either[ExtractTemplateError, Map[String, Extract]] = for
    ex <- Right(extractors)
    keys = extractors.keySet
    unused = keys.diff(matches.toSet)
    _ <-
      if unused.sizeIs == 0 then Right(extractors)
      else Left(UnusedExtracts(ex.view.filterKeys(unused.contains(_)).toMap))
  yield matches.map(m => (m, Extract.unsafe(m))).toMap ++ ex

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

  test("template has implicit extractor") {
    val template = Template("{title} by {author} on {date}")
    val implicitTitle = ("title" -> Extract.unsafe("title"))
    val implicitDate = ("date" -> Extract.unsafe("date"))
    val author = ("author" -> Extract.unsafe("author"))
    val result = ExtractTemplate.from(
      extractors = Map(author),
      template = template,
    )
    val expected =
      ExtractTemplate.unsafe(Map(author, implicitTitle, implicitDate), template)
    assert(result === Right(expected), s"got unexpected result: $result")
  }
