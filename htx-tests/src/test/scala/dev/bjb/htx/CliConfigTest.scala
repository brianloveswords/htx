package dev.bjb.htx

import cli.{*, given}
import scopt.OParser
import org.http4s.Uri

class CliConfigTest extends CommonSuite:
  test("full url, template, no mode") {
    val args = Seq("https://example.com", "{@}")
    val uri = Uri.unsafeFromString("https://example.com")
    val expected = CliConfigRaw(
      Mode.Single,
      Some(Input.Link(uri)),
      Some(TemplateEvaluator(List(Part.Pattern("@")))),
    )
    val result = CliConfigRaw.parse(args).fold(throw _, identity)
    assertEquals(result, expected)
  }

  test("partial url, template, and mode") {
    val args = Seq("example.com", "{@}", "-k", "10")
    val uri = Uri.unsafeFromString("https://example.com")
    val expected = CliConfigRaw(
      Mode.Max(10),
      Some(Input.Link(uri)),
      Some(TemplateEvaluator(List(Part.Pattern("@")))),
    )
    val result = CliConfigRaw.parse(args).fold(throw _, identity)
    assertEquals(result, expected)
  }

  test("content from stdin") {
    val args = Seq("-", "{a}")
    val expected = CliConfigRaw(
      Mode.Single,
      Some(Input.StdinContent),
      Some(TemplateEvaluator(List(Part.Pattern("a")))),
    )
    val result = CliConfigRaw.parse(args).fold(throw _, identity)
    assertEquals(result, expected)
  }

  test("links from stdin") {
    val args = Seq("@", "{a}")
    val expected = CliConfigRaw(
      Mode.Single,
      Some(Input.StdinLinks),
      Some(TemplateEvaluator(List(Part.Pattern("a")))),
    )
    val result = CliConfigRaw.parse(args).fold(throw _, identity)
    assertEquals(result, expected)
  }
