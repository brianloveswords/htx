package mdlink

import cats.effect.IO
import munit.CatsEffectSuite
import scala.concurrent.duration.*
import org.scalacheck.Prop
import org.scalacheck.Gen

class PureSoupSuite extends CommonSuite:
  val genHtmlParts: Gen[(String, String, String, String)] = Gen.zip(
    genLowerNonEmpty,
    genLowerNonEmpty,
    genLowerNonEmpty,
    genLowerNonEmpty,
  )

  val genHtmlAndMissingTag: Gen[(String, String)] = genHtmlParts flatMap {
    (tag, attr, value, text) =>
      for missing <- genLowerNonEmpty.suchThat(_ ne tag)
      yield (missing, htmlFromParts(tag, attr, value, text))
  }

  def htmlFromParts(
      tag: String,
      attr: String,
      value: String,
      text: String,
  ): String = s"""
    |<html>
    |  <body>
    |    <$tag $attr="$value"><span>$text</span></$tag>
    |  </body>
    |</html>
    """.stripMargin

  test("extract: found") {
    Prop.forAll(genHtmlParts) { (tag, attr, value, text) =>
      val soup = PureSoup(htmlFromParts(tag, attr, value, text))
      val Right(result) = Selector(tag) map soup.extract
      assertEquals(
        result,
        Some(
          Element(tag, Map(attr -> value), text),
        ),
      )
    }
  }

  test("extract: not found") {
    Prop.forAll(genHtmlAndMissingTag) { (missing, html) =>
      val soup = PureSoup(html)
      val Right(result) = Selector(missing) map soup.extract
      assertEquals(result, None)
    }
  }
