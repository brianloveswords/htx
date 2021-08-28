package dev.bjb.htx

import cli.{*, given}
import scopt.OParser
import org.http4s.Uri

class CliConfigTest extends CommonSuite:
  val builder = OParser.builder[CliConfigRaw]
  val parser1 =
    import builder.*
    OParser.sequence(
      programName("htx"),
      head("htx", "1.0.0"),
      opt[Int]('k', "num")
        .validate(k =>
          if k > 0 then success
          else failure("num must be >0"),
        )
        .action((k, c) => c.copy(num = Some(k)))
        .text("How many matches to return. When not set, it is unlimited"),
      arg[Uri]("<uri>")
        .action((uri, c) => c.copy(uri = Some(uri)))
        .text(
          "URI to pull contents. - for contents on stdin; @ for URIs",
        ),
      arg[String]("<template>")
        .action((template, c) => c.copy(template = Some(template)))
        .text(
          "URI to pull contents. - for contents on stdin; @ for URIs",
        ),
      checkConfig { c =>
        if c.uri.isEmpty then failure("uri must be set")
        else success
      },
    )

  test("works with a full url and static template") {
    val args = Seq("example")
    val (result, effects) =
      OParser.runParser(parser1, args, CliConfigRaw())

    for config <- result
    yield println(config)

    OParser.runEffects(effects)

    assert(true)
  }
