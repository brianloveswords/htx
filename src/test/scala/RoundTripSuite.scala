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

trait RoundTripSuite[T: Arbitrary: Decoder: Encoder]
    extends CatsEffectSuite
    with ScalaCheckEffectSuite:

  def roundtrip: Unit = roundtrip(assertEquals(_, _))

  def roundtrip(f: (T, T) => Unit): Unit =
    test("property: roundtrip") {
      Prop.forAllNoShrink { (t: T) =>
        val dt = decode[T](t.asJson.noSpaces)
        dt match
          case Left(e)   => fail(s"something went wrong: $e")
          case Right(dt) => f(dt, t)
      }
    }
