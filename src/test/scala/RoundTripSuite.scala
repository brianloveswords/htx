package mdlink

import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.Prop
import org.scalacheck.Arbitrary
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
