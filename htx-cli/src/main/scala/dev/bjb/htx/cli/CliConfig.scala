package dev.bjb.htx.cli

import org.http4s.Uri
import scopt.OParser
import scopt.Read

enum InputType:
  case Link(uri: Uri)
  case StdinContent
  case StdinLinks

case class CliConfigRaw(
    num: Option[Int] = None,
    uri: Option[Uri] = None,
    template: Option[String] = None,
)

given Read[Uri] = Read.reads { s =>
  val base =
    if s.startsWith("http") then s
    else if !s.contains("://") then "https://" + s
    else throw new IllegalArgumentException(s"Invalid URI")
  Uri.fromString(base) match
    case Right(uri) =>
      uri.scheme match
        case Some(Uri.Scheme.http) | Some(Uri.Scheme.https) => uri
        case _ =>
          throw new IllegalArgumentException(s"Scheme must be http(s)")
    case Left(err) => throw new IllegalArgumentException(err.toString)
}
