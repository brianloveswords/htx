package main

import cats.implicits.*
import cats.effect.IO
import org.http4s.Headers
import org.http4s.Status
import org.http4s.Uri
import org.scalacheck.Gen
import org.typelevel.ci.CIString

import java.net.URL
import scala.util.control.NoStackTrace
import scala.util.control.NonFatal
import cats.MonadThrow
import cats.Monad

case object NoUriProvided extends NoStackTrace:
  override def getMessage: String = "No URI provided"

case class MissingLocationHeader(uri: Uri, headers: Headers)
    extends NoStackTrace:
  override def toString = s"Missing Location header for $uri, headers: $headers"
object MissingLocationHeader:
  def raise[F[_]: MonadThrow, A](
      uri: Uri,
      headers: Headers,
  ): F[A] =
    MonadThrow[F].raiseError[A](MissingLocationHeader(uri, headers))

case class MalformedUri(uri: String, err: Throwable) extends NoStackTrace:
  override def getMessage: String = s"Malformed URI $uri: $err"

case class UnexpectedStatus(uri: Uri, status: Status) extends NoStackTrace:
  override def getMessage: String =
    s"Unexpected status for $uri: $status"

def ioFromOption[A](ex: Throwable)(opt: Option[A]): IO[A] =
  opt.fold(IO.raiseError(ex))(IO.pure)

def safeOption[T](a: => T): Option[T] =
  if a == null then None else Some(a)

def safeEither[T](a: => T): Either[Throwable, T] =
  try Right(a)
  catch { case NonFatal(e) => Left(e) }

def parseUri(args: List[String]): IO[Uri] =
  ioFromOption(NoUriProvided)(args.headOption).flatMap(parseUri)

def parseUri[F[_]: MonadThrow](uri: String): F[Uri] =
  def toError(err: Throwable) =
    MonadThrow[F].raiseError[Uri](MalformedUri(uri, err))

  def validate(uri: Uri) =
    try { URL(uri.toString); Monad[F].pure(uri) }
    catch { case NonFatal(err) => toError(err) }

  Uri.fromString(uri).fold(toError, Monad[F].pure).flatMap(validate)

def getHeader(name: String)(headers: Headers): Option[String] =
  headers.get(CIString(name)).map(_.head.value)

val genLowerNonEmpty = Gen.alphaLowerStr.suchThat(_.nonEmpty)
