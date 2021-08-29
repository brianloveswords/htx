package dev.bjb.htx.cli

import org.http4s.Uri
import scopt.OParser
import scopt.Read
import dev.bjb.htx.TemplateEvaluator
import scala.util.control.NoStackTrace
import scopt.DefaultOEffectSetup
import scopt.OEffect

enum InputType:
  case Link(uri: Uri)
  case StdinContent
  case StdinLinks

case class CliConfigRaw(
    num: Option[Int] = None,
    uri: Option[Uri] = None,
    template: Option[TemplateEvaluator] = None,
)

case class ArgumentsError(msg: String) extends NoStackTrace:
  override def getMessage: String = msg

object CliConfigRaw:
  val builder = OParser.builder[CliConfigRaw]
  def parse(args: Seq[String]): Either[ArgumentsError, CliConfigRaw] =
    import builder.*
    val p = OParser.sequence(
      programName("htx"),
      head("htx", "1.0.0"),
      opt[Int]('k', "num")
        .validate { k =>
          if k > 0 then success
          else failure("num must be >0")
        }
        .action((k, c) => c.copy(num = Some(k)))
        .text("How many matches to return. When not set, it is unlimited"),
      arg[Uri]("<uri>")
        .action((uri, c) => c.copy(uri = Some(uri)))
        .text(
          "URI to pull contents. - for contents on stdin; @ for URIs",
        ),
      arg[TemplateEvaluator]("<template>")
        .action((template, c) => c.copy(template = Some(template)))
        .text(
          "extraction template. Format: {<css> [ |> fn1 |> fn2 ] }",
        ),
      checkConfig { c =>
        if c.uri.isEmpty then failure("uri must be set")
        else success
      },
    )
    val (result, effects) = OParser.runParser(p, args, CliConfigRaw())
    result match
      case Some(config) => Right(config)
      case _            => Left(ArgumentsError(getMessage(effects)))

  def getMessage(effects: List[OEffect]): String =
    var message = ""
    OParser.runEffects(
      effects,
      new DefaultOEffectSetup {
        override def displayToOut(msg: String): Unit =
          message += msg + "\n"
        override def displayToErr(msg: String): Unit =
          message += msg + "\n"
        override def reportError(msg: String): Unit =
          displayToErr("Error: " + msg)
        override def reportWarning(msg: String): Unit =
          displayToErr("Warning: " + msg)
        override def terminate(exitState: Either[String, Unit]): Unit = ()
      },
    )
    message

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

given Read[TemplateEvaluator] = Read.reads { s =>
  try TemplateEvaluator.unsafe(s)
  catch
    case err: Throwable => throw new IllegalArgumentException(err.getMessage)
}
