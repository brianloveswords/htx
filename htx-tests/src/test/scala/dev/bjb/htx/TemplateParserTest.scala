package dev.bjb.htx

import org.antlr.v4.runtime.*

class TemplateParserTest extends CommonSuite:
  test("1 part, all text") {
    val parser = TemplateEvaluator("oh hello")
    assertEquals(parser.parts, Seq(Text("oh hello")))
    assertEquals(parser.patterns, Set())
  }

  test("1 pattern with spaces") {
    val parser = TemplateEvaluator("{  sup  }")
    assertEquals(parser.parts, Seq(Pattern("sup")))
    assertEquals(parser.patterns, Set("sup"))
  }

  test("1 text, 1 pattern") {
    val parser = TemplateEvaluator("oh {hello}")
    assertEquals(parser.parts, Seq(Text("oh "), Pattern("hello")))
  }

  test("1 text, 1 pattern") {
    val parser = TemplateEvaluator("oh {hello}")
    assertEquals(parser.parts, Seq(Text("oh "), Pattern("hello")))
  }

  test("1 text, with escapes") {
    val parser = TemplateEvaluator("oh \\\\{hello\\\\}")
    assertEquals(parser.parts, Seq(Text("oh \\{hello\\}")))
  }

  test("text / pattern / text") {
    val parser = TemplateEvaluator("{hi} ok {bye}")
    assertEquals(parser.parts, Seq(Pattern("hi"), Text(" ok "), Pattern("bye")))
  }

  test("eval: no replacements") {
    val parser = TemplateEvaluator("constant")
    assertEquals(parser.eval(Map.empty), "constant")
  }

  test("temp: window") {
    val xs = "hello"
    val expected = List("he", "el", "ll", "lo", "o")
    val result = pairs("hello").toList
    assertEquals(result, expected)
  }
