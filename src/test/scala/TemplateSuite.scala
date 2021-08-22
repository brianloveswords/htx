package mdlink

import io.circe.syntax.*
import io.circe.parser.*
import io.circe.generic.auto.*

class TemplateSuite extends RoundTripSuite:
  test("encode") {
    val template = Template("ok")
    val jsonString = template.asJson.noSpaces
    assertEquals(jsonString, """"ok"""")
  }

  test("decode") {
    val jsonString = """"ok""""
    decode[Template](jsonString) match
      case Left(e)  => fail(s"something went wrong: $e")
      case Right(t) => assertEquals(t, Template("ok"))
  }

  case class Inner(inner: Template)

  test("encode inner") {
    val template = Inner(Template("ok"))
    val jsonString = template.asJson.noSpaces
    assertEquals(jsonString, """{"inner":"ok"}""")
  }

  test("decode inner") {
    val jsonString = """{"inner":"ok"}"""
    decode[Inner](jsonString) match
      case Left(e)  => fail(s"something went wrong: $e")
      case Right(t) => assertEquals(t, Inner(Template("ok")))
  }

  roundtrip[Template]
