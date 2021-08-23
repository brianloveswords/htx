package mdlink

import cats.effect.IO
import munit.CatsEffectSuite
import scala.concurrent.duration.*
import org.scalacheck.Prop
import org.scalacheck.Gen

class PureSoupSuite extends CommonSuite:
  val genLowerNonEmpty = Gen.alphaLowerStr.suchThat(_.nonEmpty)

  val genHtmlParts = Gen.zip(
    genLowerNonEmpty,
    genLowerNonEmpty,
    genLowerNonEmpty,
    genLowerNonEmpty,
  )

  test("extract: found") {
    Prop.forAll(genHtmlParts) { (tag, attr, value, text) =>
      val html =
        PureSoup(
          s"""
          |<html>
          |  <body>
          |    <$tag $attr="$value"><span>$text</span></$tag>
          |  </body>
          |</html>
          |""".stripMargin,
        )
      val result = html.extract(tag)
      assertEquals(
        result,
        Right(
          Some(
            Element(tag, Map(attr -> value), text),
          ),
        ),
      )
    }
  }

  test("extract: not found") {
    val html = PureSoup("""<a href="https://www.google.com">Google</a>""")
    val result = html.extract("whatever")
    assertEquals(result, Right(None))
  }
