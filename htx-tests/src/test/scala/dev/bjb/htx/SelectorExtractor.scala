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
  val eval = TemplateEvaluator(template)
  val unsafeSelectors = tpl.patterns.map(p => (p, Selector.unsafe(p)))

  def eval(html: String): IO[List[String]] =
    val soup = PureSoup(html)
    val empty: Map[String, List[String]] = Map.empty
    unsafeSelectors
      .map { (p, selector) =>
        IO((p, soup.extract(selector).map(_.text)))
      }
      .toList
      .parSequence
      .map(_.toMap)
      .flatMap(eval.eval(_))

val tpl = TemplateEvaluator("{title} by {.author}")
val empty: ValidatedNel[Throwable, Set[Selector]] = Set().validNel
val validatedSelectors = tpl.patterns.foldLeft(empty)((acc, p) =>
  acc.combine(Selector(p).toValidatedNel.map(Set(_))),
)
val unsafeSelectors = tpl.patterns.map(Selector.unsafe(_))

class SelectorExtractorTest extends CommonSuite:
  test("title from html") {
    val ex = SelectorExtractor("{title}")
    ex.eval("<title>cool</title>") map { result =>
      assertEquals(result, List("cool"))
    }
  }
