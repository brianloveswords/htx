package dev.bjb.htx

import cats.implicits.*
import io.circe.generic.auto.*
import io.circe.parser.*

class ConfigEntryTest extends RoundTripSuite[ConfigEntry]:
  roundtrip

  test("example: detailed") {
    val jsonString = """
    |{
    |  "url": [
    |    "medium.com",
    |    "itnext.io",
    |    "blog.skyliner.io",
    |    "towardsdatascience.com",
    |    "betterprogramming.pub",
    |    "levelup.gitconnected.com"
    |  ],
    |  "extractors": {
    |    "title": {
    |      "selector": "meta[property='twitter:title']",
    |      "attribute": "content"
    |    },
    |    "author": {
    |      "selector": "meta[name='author']",
    |      "attribute": "content",
    |      "fallback": "Unknown Author"
    |    }
    |  },
    |  "template": "[{title}]({url}) by {author}"
    |}
    """.stripMargin
    val mkSelector = Selector.unsafe
    val expected = ConfigEntry(
      Matcher.Many(
        List(
          "medium.com",
          "itnext.io",
          "blog.skyliner.io",
          "towardsdatascience.com",
          "betterprogramming.pub",
          "levelup.gitconnected.com",
        ),
      ),
      Map(
        "title" -> Extractor(
          mkSelector("meta[property='twitter:title']"),
          Some("content"),
          None,
        ),
        "author" -> Extractor(
          mkSelector("meta[name='author']"),
          Some("content"),
          Some("Unknown Author"),
        ),
      ),
      Template("[{title}]({url}) by {author}"),
    )

    val Right(result) = decode[ConfigEntry](jsonString)
    assert(result === expected, s"$result !== $expected")
  }
/*
ConfigEntry(Many(List(medium.com, itnext.io, blog.skyliner.io, towardsdatascience.com, betterprogramming.pub, levelup.gitconnected.com)),Map(title -> Extract(Selector(meta[property='twitter:title'],meta[property=twitter:title]),None,Some(content)), author -> Extract(Selector(meta[name='author'],meta[name=author]),Some(Unknown Author),Some(content))),Template([{title}]({url}) by {author}))
ConfigEntry(Many(List(medium.com, itnext.io, blog.skyliner.io, towardsdatascience.com, betterprogramming.pub, levelup.gitconnected.com)),Map(title -> Extract(Selector(meta[property='twitter:title'],meta[property=twitter:title]),None,None), author -> Extract(Selector(meta[name='author'],meta[name=author]),Some(Unknown Author),None)),Template([{title}]({url}) by {author}))
 */
