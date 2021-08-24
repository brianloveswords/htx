package dev.bjb.htx.cli

import cats.implicits.*
import cats.Monad
import cats.MonadError
import cats.effect.*
import cats.effect.std.*
import org.http4s.Headers
import org.http4s.ParseFailure
import org.http4s.Status
import org.http4s.Uri
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.JavaNetClientBuilder
import org.http4s.implicits.*
import org.typelevel.ci.*
import dev.bjb.htx.*

import java.net.URL
import scala.concurrent.duration.*
import scala.util.control.NoStackTrace
import scala.util.control.NonFatal
import cats.MonadThrow

trait CLI[F[_]](using Console[F])(using Async[F]):
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

  private def extractFirstArg(args: List[String]): F[String] =
    args.headOption.fold(missingArgumentError[String])(_.pure)

  private def getHtml(uri: Uri): F[(String, Uri)] =
    client.get(uri) { resp =>
      resp.status match
        case Status.Ok => resp.as[String].map((_, uri))
        case Status.MovedPermanently =>
          val headers = resp.headers
          lazy val error: F[String] = MissingLocationHeader.raise(uri, headers)
          getLocationHeader(headers)
            .fold(error)(_.pure)
            .flatMap(parseUri)
            .flatTap(showRedirect)
            .flatMap(getHtml)
        case status => MonadThrow[F].raiseError(UnexpectedStatus(uri, status))
    }

  def run(args: List[String]): F[ExitCode] =
    val program = for
      uri <- extractFirstArg(args) flatMap parseUri
      _ <- Console[F].errorln(s"url: $uri")
      result <- getHtml(uri)
      (html, uri) = result
      soup = PureSoup(html)
      selector = Selector.unsafe("title")
      element = soup.extract(selector)
      title = element.fold("<title not found>")(_.text)
      _ <- Console[F].print(s"[$title]($uri)")
    yield ExitCode.Success
    Async[F].timeout(program.handleErrorWith(printErrorAndExit), 10.seconds)

object mdlink extends IOApp, CLI[IO]
