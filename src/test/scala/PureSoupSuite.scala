package mdlink

import cats.effect.IO
import munit.CatsEffectSuite
import scala.concurrent.duration.*
import org.scalacheck.Prop
import org.scalacheck.Gen
import org.scalacheck.rng.Seed

class PureSoupSuite extends CommonSuite:
  val genTag: Gen[String] =
    genLowerNonEmpty map ("x-" + _)

  val genHtmlParts: Gen[(String, String, String, String)] = Gen.zip(
    genTag,
    genLowerNonEmpty,
    genLowerNonEmpty,
    genLowerNonEmpty,
  )

  val genHtmlAndMissingTag: Gen[(String, String)] = genHtmlParts flatMap {
    (tag, attr, value, text) =>
      for missing <- genTag.suchThat(_ ne tag)
      yield (missing, htmlFromParts(tag, attr, value, text))
  }

  def htmlFromParts(
      tag: String,
      attr: String,
      value: String,
      text: String,
  ): String = s"""<$tag $attr="$value"><span>$text</span></$tag>""".stripMargin

  test("extract: found") {
    Prop.forAllNoShrink(genHtmlParts) { (tag, attr, value, text) =>
      val html = htmlFromParts(tag, attr, value, text)
      val soup = PureSoup(html)
      val Right(result) = Selector(tag) map soup.extract
      assertEquals(
        result,
        Some(Element(tag, Map(attr -> value), text)),
        s"with soup: $soup",
      )
    }
  }

  test("extract: not found") {
    Prop.forAllNoShrink(genHtmlAndMissingTag) { (missing, html) =>
      val soup = PureSoup(html)
      val Right(result) = Selector(missing) map soup.extract
      assertEquals(result, None, s"with soup: $soup")
    }
  }
