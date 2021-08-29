package dev.bjb.htx.cli

import dev.bjb.htx.TemplateEvaluator
import org.http4s.Uri
import scopt.DefaultOEffectSetup
import scopt.OEffect
import scopt.OParser
import scopt.Read

import scala.util.control.NoStackTrace

enum Input:
  case Link(uri: Uri)
  case StdinContent
  case StdinLinks

enum Mode:
  case Single
  case Max(k: Int)

case class CliConfigRaw(
    mode: Mode = Mode.Single,
    input: Option[Input] = None,
    template: Option[TemplateEvaluator] = None,
)

case class CliConfig(
    mode: Mode,
    input: Input,
    template: TemplateEvaluator,
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
      opt[Int]('k', "max")
        .validate { k =>
          if k > 0 then success
          else failure("max must be >0")
        }
        .action((k, c) => c.copy(mode = Mode.Max(k)))
        .text("How many matches to return. When not set, it is unlimited"),
      arg[Input]("<uri>")
        .action((input, c) => c.copy(input = Some(input)))
        .text(
          "URI to pull contents. - for contents on stdin; @ for URIs",
        ),
      arg[TemplateEvaluator]("<template>")
        .action((template, c) => c.copy(template = Some(template)))
        .text(
          "extraction template. Format: {<css> [ |> fn1 |> fn2 ] }",
        ),
      help("help").text("prints this usage text"),
      checkConfig { c =>
        if c.input.isEmpty then failure("uri must be set")
        else success
      },
    )
    // scopt doesn't seem to play well with args that are a single '-', so we
    // convert it to something else before parsing.
    val fixedArgs = args.map(s => if s == "-" then "%" else s)

    val (result, effects) = OParser.runParser(p, fixedArgs, CliConfigRaw())
    result match
      case Some(config) => Right(config)
      case _            => Left(ArgumentsError(getMessage(effects)))

  def getMessage(effects: List[OEffect]): String =
    var message = ""
    OParser.runEffects(
      effects,
      new DefaultOEffectSetup {
        // for certain situations (e.g. --help), effects will continue even
        // after termination. we don't want those effects to be included in
        // the final message, so we keep track of liveness and stop appending
        // when terminate has been called
        var live = true
        override def displayToOut(msg: String): Unit =
          if live then message += msg + "\n"
        override def displayToErr(msg: String): Unit =
          if live then message += msg + "\n"
        override def reportError(msg: String): Unit =
          displayToErr("Error: " + msg)
        override def reportWarning(msg: String): Unit =
          displayToErr("Warning: " + msg)
        override def terminate(exitState: Either[String, Unit]): Unit =
          live = false
      },
    )
    message

//
// Read instances
//

given Read[Input] = Read.reads { s =>
  s match
    case "%" | "-" => Input.StdinContent
    case "@"       => Input.StdinLinks
    case uri =>
      val uri = summon[Read[Uri]].reads(s)
      Input.Link(uri)
}

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
