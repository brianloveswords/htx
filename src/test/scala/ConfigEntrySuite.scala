package mdlink

import cats.effect.IO
import cats.effect.SyncIO
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.Gen
import org.scalacheck.Prop
import org.scalacheck.Arbitrary
import org.scalacheck.effect.PropF
import io.circe.Decoder
import io.circe.Encoder
import io.circe.syntax.*
import io.circe.parser.*
import io.circe.generic.auto.*

class ConfigEntrySuite extends RoundTripSuite[ConfigEntry]:
  roundtrip

  test("example: basic") {
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
    val result = decode[ConfigEntry](jsonString)
    println(result)
  }
