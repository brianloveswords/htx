package dev.bjb.htx

import org.antlr.v4.runtime.*

class TemplateParserTest extends CommonSuite:
  test("1 part, all text") {
    val parser = TemplateEvaluator("oh hello")
    assertEquals(parser.parts, Seq(Text("oh hello")))
  }
