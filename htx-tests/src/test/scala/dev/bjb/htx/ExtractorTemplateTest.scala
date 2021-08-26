package dev.bjb.htx

import cats.implicits.*

class ExtractorTemplateTest extends CommonSuite:
  import ExtractorTemplateError.*

  test("no replacements") {
    val template = Template("cool")
    val result = ExtractorTemplate.from(
      extractors = Map(),
      template = template,
      uri = None,
    )
    assertEquals(result, Left(NoReplacements(template)))
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
    assertEquals(result, Left(UnusedExtracts(Map(title))))
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
    val expected = Right(
      ExtractorTemplate.unsafe(
        extractors = Map(author, implicitTitle, implicitDate),
        template = template,
        uri = None,
      ),
    )
    assert(result === expected, s"got unexpected result: $result")
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
