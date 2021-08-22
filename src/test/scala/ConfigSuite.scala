package mdlink

import cats.effect.IO
import cats.effect.SyncIO
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.Gen
import org.scalacheck.Prop
import org.scalacheck.Arbitrary
import org.scalacheck.effect.PropF
import io.circe.Decoder
import io.circe.Encoder
import io.circe.syntax.*
import io.circe.parser.*
import io.circe.generic.auto.*

trait RoundTripSuite extends CatsEffectSuite with ScalaCheckEffectSuite:
  def roundtrip[T: Arbitrary: Decoder: Encoder]: Unit =
    test("property: roundtrip") {
      Prop.forAll { (t: T) =>
        val dt = decode[T](t.asJson.noSpaces)
        dt match
          case Left(e)   => fail(s"something went wrong: $e")
          case Right(dt) => assertEquals(t, dt)
      }
    }

class ConfigTemplateSuite extends RoundTripSuite:
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
