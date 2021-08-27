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

  test("eval: one pattern, missing") {
    val parser = TemplateEvaluator("{404}")
    parser.eval(Map.empty) map { result =>
      assertEquals(result, List("<missing: 404>"))
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

  test("eval: two patterns, two replacements") {
    val parser = TemplateEvaluator("{a}{b}")
    val replacements = Map(
      "a" -> List("a1", "a2"),
      "b" -> List("b1", "b2"),
    )
    val expected = List(
      "a1b1",
      "a2b2",
      "a1b1",
      "a2b2",
    )
    parser.eval(replacements) map { result =>
      assertEquals(result, expected)
    }
  }

  test("eval: three patterns, non-symmetrical") {
    val parser = TemplateEvaluator("{a}{b}{c}{d}")
    val replacements = Map(
      "a" -> List("a1", "a2", "a3"),
      "b" -> List("b1"),
      "c" -> List("c1", "c2"),
      "d" -> List("d1", "d2"),
    )
    val expected = List(
      "a1b1c1d1",
      "a2b1c2d2",
      "a3b1c1d1",
      "a1b1c2d2",
      "a2b1c1d1",
      "a3b1c2d2",
      "a1b1c1d1",
      "a2b1c2d2",
      "a3b1c1d1",
      "a1b1c2d2",
      "a2b1c1d1",
      "a3b1c2d2",
    )
    parser.eval(replacements) map { result =>
      assertEquals(result, expected)
    }
  }
