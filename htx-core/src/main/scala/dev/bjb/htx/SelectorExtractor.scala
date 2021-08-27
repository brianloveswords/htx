package dev.bjb.htx

import cats.implicits.*
import cats.data.ValidatedNel
import cats.data.Validated.Valid
import cats.effect.IO
import cats.effect.Async
import cats.effect.Concurrent
import cats.Parallel

case class SelectorExtractor(template: String):
  lazy val tpl = TemplateEvaluator(template)
  lazy val unsafeSelectors = tpl.patterns.map(p => (p, Selector.unsafe(p)))
  lazy val empty: ValidatedNel[Throwable, Set[Selector]] = Set().validNel
  lazy val validatedSelectors = tpl.patterns.foldLeft(empty)((acc, p) =>
    acc.combine(Selector(p).toValidatedNel.map(Set(_))),
  )

  def eval[F[_]: Async: Concurrent: Parallel](html: String): F[List[String]] =
    val soup = PureSoup(html)
    val empty: Map[String, List[String]] = Map.empty
    unsafeSelectors
      .map { (p, selector) =>
        Async[F].delay(soup.extract(selector).map(_.text)) map { extracts =>
          (p, if extracts.sizeIs == 0 then List(s"<missing: $p") else extracts)
        }
      }
      .toList
      .parSequence
      .map(_.toMap)
      .flatMap(tpl.eval(_))
