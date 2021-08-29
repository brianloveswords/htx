package dev.bjb.htx.cli

import cats.Monad
import cats.MonadError
import cats.MonadThrow
import cats.Parallel
import cats.effect.*
import cats.effect.std.*
import cats.implicits.*
import dev.bjb.htx.*
import org.http4s.Headers
import org.http4s.ParseFailure
import org.http4s.Status
import org.http4s.Uri
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.JavaNetClientBuilder
import org.http4s.implicits.*
import org.typelevel.ci.*

import java.net.URL
import scala.concurrent.duration.*
import scala.util.control.NoStackTrace
import scala.util.control.NonFatal
import scala.io.Source

trait Cli[F[_]](using Console[F])(using Async[F], Parallel[F]):
  private val client = JavaNetClientBuilder[F].create
  private def getLocationHeader = getHeader("location")

  private def missingArgumentError[A] =
    MonadThrow[F].raiseError[A](
      IllegalArgumentException("No argument provided"),
    )

  private def showRedirect(uri: Uri): F[Unit] =
    Console[F].errorln(s"following redirect: $uri")

  private def printErrorAndExit(err: Throwable): F[ExitCode] =
    Console[F].errorln(err.getMessage).as(ExitCode.Error)

  private def filterOpts(args: List[String]): List[String] =
    args.filter(!_.startsWith("-"))

  private def extractFirstArg(args: List[String]): F[String] =
    filterOpts(args).headOption
      .fold(missingArgumentError[String])(_.pure)

  private def extractSecondArg(args: List[String]): F[String] =
    extractFirstArg(filterOpts(args).tail)

  private def includeNewLine(args: List[String]): Boolean =
    !args.contains("-n")

  private def getHtml(input: Input): F[(String, Option[Uri])] =
    import Input.*
    input match
      case Link(uri) =>
        client.get(uri) { resp =>
          resp.status match
            case Status.Ok => resp.as[String].map((_, Some(uri)))
            case Status.MovedPermanently =>
              val headers = resp.headers
              lazy val error: F[String] =
                MissingLocationHeader.raise(uri, headers)
              getLocationHeader(headers)
                .fold(error)(_.pure)
                .flatMap(loc =>
                  if loc.startsWith("https://") then parseUri(loc)
                  // TODO: this does not work right: need to check if loc is
                  // relative or absolute and then combine it accordingly
                  else parseUri(uri.toString + loc),
                )
                .flatTap(showRedirect)
                .flatMap(uri => getHtml(Input.Link(uri)))
            case status =>
              MonadThrow[F].raiseError(UnexpectedStatus(uri, status))
        }
      case StdinContent =>
        Source
          .fromInputStream(System.in)
          .getLines
          .mkString("\n")
          .pure
          .map((_, None))
      case StdinLinks =>
        throw new NotImplementedError("stdin links not implemented yet")

  def getConfig(args: List[String]): F[CliConfig] =
    CliConfig.parse(args).fold(MonadThrow[F].raiseError, _.pure)

  def run(args: List[String]): F[ExitCode] =
    val program = for
      config <- getConfig(args)
      mode = config.mode
      input = config.input
      template = config.template
      ex = SelectorExtractor[F](template)
      newLine = if includeNewLine(args) then "\n" else ""
      _ <- sys.env.get("DEBUG").fold(Monad[F].pure(())) { _ =>
        val message = Seq(
          s"DEBUG: args: $args",
          s"DEBUG: mode: $mode",
          s"DEBUG: input: $input",
          s"DEBUG: template: $template",
          s"DEBUG: newline: $newLine",
          "-" * 72,
        ).mkString("\n")
        Console[F].errorln(message)
      }
      result <- getHtml(input) flatMap ex.eval
      formatted = result.mkString("\n") + newLine
      _ <- Console[F].print(formatted)
    yield ExitCode.Success

    Async[F].timeout(program.handleErrorWith(printErrorAndExit), 10.seconds)

object Main extends IOApp, Cli[IO]
