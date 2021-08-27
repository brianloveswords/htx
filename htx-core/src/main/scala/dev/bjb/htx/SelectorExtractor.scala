package dev.bjb.htx

import cats.implicits.*
import cats.data.ValidatedNel
import cats.data.Validated.Valid
import cats.effect.IO
import cats.effect.Async
import cats.effect.Concurrent
import cats.Parallel
import org.http4s.Uri

case class SelectorExtractor(template: String):
  lazy val tpl = TemplateEvaluator(template)
  lazy val unsafeSelectors =
    tpl.patterns.filter(_ != "@").map(p => (p, Selector.unsafe(p)))

  lazy val empty: ValidatedNel[Throwable, Set[Selector]] = Set().validNel
  lazy val validatedSelectors = tpl.patterns.foldLeft(empty)((acc, p) =>
    acc.combine(Selector(p).toValidatedNel.map(Set(_))),
  )

  def eval[F[_]: Async: Concurrent: Parallel](
      html: String,
      uri: Option[Uri] = None,
  ): F[List[String]] =
    val soup = PureSoup(html)
    val autoVars: Map[String, List[String]] =
      uri.map(u => Map("@" -> List(u.toString))).getOrElse(Map.empty)

    unsafeSelectors
      .map { (p, selector) =>
        Async[F].delay(soup.extract(selector).map(_.text)) map { extracts =>
          (p, if extracts.sizeIs == 0 then List(s"<missing: $p") else extracts)
        }
      }
      .toList
      .parSequence
      .map(_.toMap ++ autoVars)
      .flatMap(tpl.eval(_))
