package dev.bjb.htx

import cli.{*, given}
import scopt.OParser
import org.http4s.Uri

class CliConfigTest extends CommonSuite:
  test("works with a full url and static template") {
    val args = Seq("example", "{@")
    val result = CliConfigRaw.parse(args)
    println(result)
    assert(true)
  }
