package dev.bjb.htx

import cats.implicits.*
import org.http4s.implicits.*

class ReplacerTest extends CommonSuite:
  import ReplacerError.*
  import ReplaceUnit.*

  test("no replacements") {
    val template = Template("cool")
    val result = Replacer.from(
      extractors = Map(),
      template = template,
      uri = None,
    )
    val expected: ReplacerResult = Left(NoReplacements(template))
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
    val result = Replacer.from(
      extractors = Map(title, author),
      template = template,
      uri = None,
    )
    val expected: ReplacerResult = Left(UnusedExtracts(Map(title)))
    assertEq(result, expected)
  }

  test("template has implicit extractor") {
    val template = Template("{title} by {author} on {date}")
    val title = ("title" -> Extractor.unsafe("title"))
    val date = ("date" -> Extractor.unsafe("date"))
    val author = ("author" -> Extractor.unsafe("author"))
    val result = Replacer.from(
      extractors = Map(author),
      template = template,
      uri = None,
    )
    val expected: ReplacerResult = Right(
      Replacer.unsafe(
        replacements = Map(
          title.toReplaceMapEntry(Offset(0, 7)),
          author.toReplaceMapEntry(Offset(11, 19)),
          date.toReplaceMapEntry(Offset(23, 29)),
        ),
        template = template,
        uri = None,
      ),
    )

    assertEq(result, expected)
  }

  test("template has implicit extractor, but it's bad") {
    val template = Template("x{ti~!~tle}x")
    val result = Replacer.from(
      extractors = Map(),
      template = template,
      uri = None,
    )
    val expected: ReplacerResult = Left(
      InvalidSelectorFromTemplate(
        template,
        "ti~!~tle",
        "Could not parse query '!': unexpected token at '!'",
      ),
    )
    assertEq(result, expected)
  }

  test("template has URL autovar") {
    val template = Template("{@}")
    val uri = uri"https://example.com"
    val result = Replacer.from(
      extractors = Map(),
      template = template,
      uri = Some(uri),
    )
    val expected: ReplacerResult = Right(
      Replacer.unsafe(
        replacements = Map("@" -> AutoUri(Offset(0, 3), uri)),
        template = template,
        uri = Some(uri),
      ),
    )
    assertEq(result, expected)
  }

  test("replacer did not find anything and didn't have fallbacks") {
    val template = Template("{title}")
    val soup = PureSoup("<title>ok</title>")
    val Right(replacer) = Replacer.from(
      extractors = Map(),
      template = template,
      uri = None,
    )
    assertEq(replacer(soup), none[String])
  }
