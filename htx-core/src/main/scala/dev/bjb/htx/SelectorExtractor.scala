package dev.bjb.htx

import cats.implicits.*
import cats.data.ValidatedNel
import cats.data.Validated.Valid
import cats.effect.IO
import cats.effect.Async
import cats.effect.Concurrent
import cats.Parallel
import org.http4s.Uri
import cats.Monad
import fs2.Stream
import io.github.vigoo.prox.ProxFS2
import fs2.text
import fs2.Chunk

case class Evaluator[F[_]: Async](
    key: String,
    selector: Selector,
    fn: String => F[String],
)

def compose[F[_]: Async](
    fa: String => F[String],
    fb: String => F[String],
): String => F[String] =
  (s: String) => fa(s).flatMap(fb)

def id[F[_]: Async]: String => F[String] =
  (s: String) => Async[F].pure(s)

case class SelectorExtractor[F[_]: Async: Concurrent: Parallel](
    template: String,
):
  val fnMap: Map[String, String => F[String]] = Map(
    "upper" -> (s => s.toUpperCase.pure[F]),
    "lower" -> (s => s.toLowerCase.pure[F]),
    "trim" -> (s => s.trim.pure[F]),
  )

  private def runScript(script: String, input: String): F[String] =
    //TODO: there is probably a less dumb way to do this
    val prox = ProxFS2[F]
    import prox.*
    val stdin = Stream.chunk(Chunk.array(input.getBytes))
    val stdout = fs2.text.utf8.decode[F]
    val proc = (proc"node $script" < stdin) ># stdout
    val result =
      given ProcessRunner[JVMProcessInfo] = new JVMProcessRunner()
      proc.run()
    result.map(info => info.output)

  private def getFn(name: String): String => F[String] =
    if name.endsWith("js") then (s: String) => runScript(name, s)
    else fnMap(name)

  lazy val tpl = TemplateEvaluator(template)
  lazy val unsafeEvaluators: Set[Evaluator[F]] =
    tpl.patterns
      .filter(_ != "@")
      .map(key => {
        if key.contains("|>") then
          val parts = key.split("\\|>").toList
          val selector = Selector.unsafe(parts.head)
          val fns = parts.tail.map(name => getFn(name.trim))
          val fn = fns.reduce(compose)
          Evaluator(key, selector, fn)
        else Evaluator[F](key, Selector.unsafe(key), Monad[F].pure)
      })

  lazy val empty: ValidatedNel[Throwable, Set[Selector]] = Set().validNel
  lazy val validatedSelectors = tpl.patterns.foldLeft(empty)((acc, p) =>
    acc.combine(Selector(p).toValidatedNel.map(Set(_))),
  )

  def eval(
      html: String,
      uri: Option[Uri] = None,
  ): F[List[String]] =
    val soup = PureSoup(html)
    val autoVars: Map[String, List[String]] =
      uri.map(u => Map("@" -> List(u.toString))).getOrElse(Map.empty)

    unsafeEvaluators
      .map { evaluator =>
        val Evaluator(key, selector, fn) = evaluator
        Async[F].delay(soup.extract(selector).map(_.text)) flatMap { extracts =>
          if extracts.sizeIs == 0 then
            Async[F].pure(key, List(s"<missing: $key>"))
          else extracts.map(fn).parSequence.map(ex => key -> ex)
        }
      }
      .toList
      .parSequence
      .map(_.toMap ++ autoVars)
      .flatMap(tpl.eval(_))
