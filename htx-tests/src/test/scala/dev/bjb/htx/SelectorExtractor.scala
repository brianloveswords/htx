package dev.bjb.htx

package dev.bjb.htx

import cats.implicits.*
import io.circe.syntax.*
import io.circe.parser.*
import io.circe.generic.auto.*
import cats.data.ValidatedNel
import cats.data.Validated.Valid
import cats.effect.IO

case class SelectorExtractor(template: String):
  lazy val tpl = TemplateEvaluator(template)
  lazy val unsafeSelectors = tpl.patterns.map(p => (p, Selector.unsafe(p)))
  lazy val empty: ValidatedNel[Throwable, Set[Selector]] = Set().validNel
  lazy val validatedSelectors = tpl.patterns.foldLeft(empty)((acc, p) =>
    acc.combine(Selector(p).toValidatedNel.map(Set(_))),
  )

  def eval(html: String): IO[List[String]] =
    val soup = PureSoup(html)
    val empty: Map[String, List[String]] = Map.empty
    unsafeSelectors
      .map { (p, selector) =>
        IO(soup.extract(selector).map(_.text)) map { extracts =>
          (p, if extracts.sizeIs == 0 then List(s"<missing: $p") else extracts)
        }
      }
      .toList
      .parSequence
      .map(_.toMap)
      .flatMap(tpl.eval(_))

class SelectorExtractorTest extends CommonSuite:
  test("title from html") {
    val ex = SelectorExtractor("{title}")
    ex.eval("<title>cool</title>") map { result =>
      assertEquals(result, List("cool"))
    }
  }
  test("multiple matching elements") {
    val html = """
    |<title>site</title>
    |<h2 class=author>A1</h2>
    |<h2 class=author>A2</h2>
    """.stripMargin

    val ex = SelectorExtractor("{title}: {.author}")
    ex.eval(html) map { result =>
      assertEquals(result, List("site: A1", "site: A2"))
    }
  }
