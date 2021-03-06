package dev.bjb.htx

import cli.{*, given}
import scopt.OParser
import org.http4s.Uri

class CliConfigTest extends CommonSuite:
  test("full url, template, no mode") {
    val args = Seq("{@}", "https://example.com")
    val uri = Uri.unsafeFromString("https://example.com")
    val expected = CliConfig(
      Mode.All,
      Input.Link(uri),
      TemplateEvaluator(List(Part.Pattern("@"))),
    )
    val result = CliConfig.parse(args).fold(throw _, identity)
    assertEquals(result, expected)
  }

  test("partial url, template, and mode") {
    val args = Seq("{@}", "example.com", "-k", "10")
    val uri = Uri.unsafeFromString("https://example.com")
    val expected = CliConfig(
      Mode.Max(10),
      Input.Link(uri),
      TemplateEvaluator(List(Part.Pattern("@"))),
    )
    val result = CliConfig.parse(args).fold(throw _, identity)
    assertEquals(result, expected)
  }

  test("content from stdin") {
    val args = Seq("{a}")
    val expected = CliConfig(
      Mode.All,
      Input.StdinContent,
      TemplateEvaluator(List(Part.Pattern("a"))),
    )
    val result = CliConfig.parse(args).fold(throw _, identity)
    assertEquals(result, expected)
  }

  test("links from stdin") {
    val args = Seq("{a}", "@")
    val expected = CliConfig(
      Mode.All,
      Input.StdinLinks,
      TemplateEvaluator(List(Part.Pattern("a"))),
    )
    val result = CliConfig.parse(args).fold(throw _, identity)
    assertEquals(result, expected)
  }

  test("help") {
    val args = Seq("--help")
    val result = CliConfig
      .parse(args)
      .fold(identity, v => throw new Exception(s"unexpected result: $v"))
    val message = result.getMessage
    val containsUsage = message.contains("Usage:")
    val doesNotContainError = !message.contains("Error:")
    assert(containsUsage)
    assert(doesNotContainError)
  }
