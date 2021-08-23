package mdlink

import org.scalacheck.Prop
import org.scalacheck.Arbitrary
import io.circe.Decoder
import io.circe.Encoder
import io.circe.syntax.*
import io.circe.parser.*
import io.circe.generic.auto.*
import cats.implicits.*
import cats.Eq

trait RoundTripSuite[T: Arbitrary: Decoder: Encoder: Eq] extends CommonSuite:
  def roundtrip: Unit = roundtrip { (a, b) => assert(a === b) }

  def roundtrip(f: (T, T) => Unit): Unit =
    test("property: roundtrip") {
      Prop.forAllNoShrink { (t: T) =>
        val dt = decode[T](t.asJson.noSpaces)
        dt match
          case Left(e)   => fail(s"something went wrong: $e")
          case Right(dt) => f(dt, t)
      }
    }
