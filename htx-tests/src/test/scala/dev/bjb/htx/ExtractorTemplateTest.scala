package dev.bjb.htx

import cats.implicits.*
import org.http4s.implicits.*

class ExtractorTemplateTest extends CommonSuite:
  import ExtractorTemplateError.*
  import ReplacementEntry.*

  def extractorToReplacement(
      extractorEntry: (String, Extractor),
  ): (String, ReplacementEntry) =
    extractorEntry match
      case (key, extractor) => (key, Standard(extractor))

  test("no replacements") {
    val template = Template("cool")
    val result = ExtractorTemplate.from(
      extractors = Map(),
      template = template,
      uri = None,
    )
    val expected: ExtractorTemplateResult = Left(NoReplacements(template))
    assertEq(result, expected)
  }

  test("unused extracts") {
    val template = Template("x{author}x")
    val title = ("title" -> Extractor(Selector.unsafe("h1.title")))
    val author = ("author" -> Extractor(
      Selector.unsafe("meta[name='author']"),
      Some("content"),
      Some("Unknown Author"),
    ))
    val result = ExtractorTemplate.from(
      extractors = Map(title, author),
      template = template,
      uri = None,
    )
    val expected: ExtractorTemplateResult = Left(UnusedExtracts(Map(title)))
    assertEq(result, expected)
  }

  test("template has implicit extractor") {
    val template = Template("{title} by {author} on {date}")
    val implicitTitle = ("title" -> Extractor.unsafe("title"))
    val implicitDate = ("date" -> Extractor.unsafe("date"))
    val author = ("author" -> Extractor.unsafe("author"))
    val result = ExtractorTemplate.from(
      extractors = Map(author),
      template = template,
      uri = None,
    )
    val expected: ExtractorTemplateResult = Right(
      ExtractorTemplate.unsafe(
        replacements = Map(
          implicitTitle,
          author,
          implicitDate,
        ).map(extractorToReplacement),
        template = template,
        uri = None,
      ),
    )

    assertEq(result, expected)

  }

  test("template has implicit extractor, but it's bad") {
    val template = Template("x{ti~!~tle}x")
    val result = ExtractorTemplate.from(
      extractors = Map(),
      template = template,
      uri = None,
    )
    val expected = Left(
      InvalidSelectorFromTemplate(
        template,
        "ti~!~tle",
        "Could not parse query '!': unexpected token at '!'",
      ),
    )
    assertEquals(result, expected)
  }

  test("template has URL autovar") {
    val template = Template("{@}")
    val uri = uri"https://example.com"
    val result = ExtractorTemplate.from(
      extractors = Map(),
      template = template,
      uri = Some(uri),
    )
    val expected = Right(
      ExtractorTemplate.unsafe(
        replacements = Map("@" -> AutoUri(uri)),
        template = template,
        uri = Some(uri),
      ),
    )
    assertEquals(result, expected)
  }
