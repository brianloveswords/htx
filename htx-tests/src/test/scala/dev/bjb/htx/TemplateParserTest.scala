package dev.bjb.htx

import org.antlr.v4.runtime.*

class TemplateParserTest extends CommonSuite:
  test("1 part, all text") {
    val parser = TemplateEvaluator("oh hello")
    assertEquals(parser.parts, Seq(Text("oh hello")))
  }

  test("1 text, 1 pattern") {
    val parser = TemplateEvaluator("oh {hello}")
    assertEquals(parser.parts, Seq(Text("oh "), Pattern("hello")))
  }

  test("1 text, with escapes") {
    val parser = TemplateEvaluator("oh \\{hello\\}")
    assertEquals(parser.parts, Seq(Text("oh {hello}")))
  }

  test("temp: window") {
    val xs = "hello"
    val expected = List("he", "el", "ll", "lo", "o")
    val result = pairs("hello").toList
    assertEquals(result, expected)
  }
