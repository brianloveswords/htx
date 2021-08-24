package mdlink
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

import java.net.URL
import scala.concurrent.duration.*
import scala.util.control.NoStackTrace
import scala.util.control.NonFatal

case object NoUriProvided extends NoStackTrace:
  override def getMessage: String = "No URI provided"

case class MissingLocationHeader(uri: Uri, headers: Headers)
    extends NoStackTrace:
  override def toString = s"Missing Location header for $uri, headers: $headers"

case class MalformedUri(uri: String, err: Throwable) extends NoStackTrace:
  override def getMessage: String = s"Malformed URI $uri: $err"

case class UnexpectedStatus(uri: Uri, status: Status) extends NoStackTrace:
  override def getMessage: String =
    s"Unexpected status for $uri: $status"

private def parseUri(args: List[String]): IO[Uri] =
  ioFromOption(NoUriProvided)(args.headOption).flatMap(parseUri)

private def parseUri(uri: String): IO[Uri] =
  def toError(err: Throwable) = IO.raiseError(MalformedUri(uri, err))
  def confirm(uri: Uri) =
    try { URL(uri.toString); IO.pure(uri) }
    catch { case NonFatal(err) => toError(err) }

  Uri.fromString(uri).fold(toError, IO.pure).flatMap(confirm)

private def getHeader(name: String)(headers: Headers): Option[String] =
  headers.get(CIString(name)).map(_.head.value)

object mdlink extends IOApp:
  def getLocationHeader = getHeader("location")
  val client = JavaNetClientBuilder[IO].create

  private def showRedirect(uri: Uri): IO[Unit] =
    Console[IO].errorln(s"following redirect: $uri")

  def getHtml(uri: Uri): IO[(String, Uri)] =
    client.get(uri) { resp =>
      resp.status match
        case Status.Ok => resp.as[String].map((_, uri))
        case Status.MovedPermanently =>
          val headers = resp.headers
          lazy val error = IO.raiseError(MissingLocationHeader(uri, headers))
          getLocationHeader(headers)
            .fold(error)(IO.pure)
            .flatMap(parseUri)
            .flatTap(showRedirect)
            .flatMap(getHtml)
        case status => IO.raiseError(UnexpectedStatus(uri, status))
    }

  def run(args: List[String]): IO[ExitCode] = {
    for
      uri <- parseUri(args)
      _ <- Console[IO].errorln(s"url: $uri")
      result <- getHtml(uri)
      (html, uri) = result
      soup = PureSoup(html)
      selector = Selector.unsafe("title")
      element = soup.extract(selector)
      title = element.fold("<title not found>")(_.text)
      _ <- Console[IO].print(s"[$title]($uri)")
    yield ExitCode.Success
  } handleErrorWith { err =>
    Console[IO].errorln(err.getMessage).as(ExitCode.Error)
  } timeout (5.seconds)
