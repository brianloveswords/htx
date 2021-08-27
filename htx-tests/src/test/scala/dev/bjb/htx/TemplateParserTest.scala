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
    parser.eval(Map.empty) map { result =>
      assertEquals(result, List("constant"))
    }
  }

  test("eval: one pattern, one replacement") {
    val parser = TemplateEvaluator("x{ a }x")
    parser.eval(Map("a" -> List("1"))) map { result =>
      assertEquals(result, List("x1x"))
    }
  }

  test("eval: one pattern, two replacements") {
    val parser = TemplateEvaluator("x{ a }x")
    parser.eval(Map("a" -> List("1", "2"))) map { result =>
      assertEquals(result, List("x1x", "x2x"))
    }
  }
