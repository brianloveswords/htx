package dev.bjb.htx

import cli.*
import scopt.OParser

class CliConfigTest extends CommonSuite:
  val builder = OParser.builder[CliConfigRaw]
  val parser1 =
    import builder.*
    OParser.sequence(
      programName("htx"),
      head("htx", "1.0.0"),
      opt[Int]('k', "num")
        .validate(k =>
          if k > 0 then Left("num must be greater than 0")
          else Right(()),
        )
        .action((k, c) => c.copy(num = Some(k)))
        .text("How many matches to return. When not set, it is unlimited"),
    )

  test("hello") {
    assert(true)
  }
