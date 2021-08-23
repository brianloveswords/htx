package mdlink

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
    val result = decode[ConfigEntry](jsonString)
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
        "title" -> Extract("meta[property='twitter:title']", None),
        "author" -> Extract("meta[name='author']", Some("Unknown Author")),
      ),
      Template("[{title}]({url}) by {author}"),
    )
    assert(result === Right(expected))
  }
