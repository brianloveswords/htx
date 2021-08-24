package main

import cats.implicits.*
import io.circe.generic.auto.*
import io.circe.parser.*

class ConfigEntrySuite extends RoundTripSuite[ConfigEntry]:
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
    |  "extract": {
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
        "title" -> Extract(
          mkSelector("meta[property='twitter:title']"),
          None,
        ),
        "author" -> Extract(
          mkSelector("meta[name='author']"),
          Some("Unknown Author"),
        ),
      ),
      Template("[{title}]({url}) by {author}"),
    )

    val Right(result) = decode[ConfigEntry](jsonString)
    assert(result === expected, s"$result !== $expected")
  }
