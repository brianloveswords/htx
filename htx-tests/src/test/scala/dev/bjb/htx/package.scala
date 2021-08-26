package dev.bjb.htx

import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.Prop
import org.scalacheck.Arbitrary
import io.circe.Decoder
import io.circe.Encoder
import io.circe.syntax.*
import io.circe.parser.*
import io.circe.generic.auto.*
import cats.implicits.*
import cats.Eq
import munit.Location
import munit.internal.console.StackTraces
import munit.internal.difflib.Diffs
import munit.internal.difflib.ComparisonFailExceptionHandler

trait CommonSuite extends CatsEffectSuite with ScalaCheckEffectSuite:
  private def munitComparisonHandler(
      actualObtained: Any,
      actualExpected: Any,
  ): ComparisonFailExceptionHandler =
    new ComparisonFailExceptionHandler {
      override def handle(
          message: String,
          unusedObtained: String,
          unusedExpected: String,
          loc: Location,
      ): Nothing = failComparison(message, actualObtained, actualExpected)(loc)
    }

  def assertEq[A: Eq, B: Eq](
      obtained: A,
      expected: B,
      clue: => Any = "values are not the same",
  )(implicit loc: Location, ev: B <:< A): Unit =
    StackTraces.dropInside {
      if (!(obtained === expected))
        Diffs.assertNoDiff(
          munitPrint(obtained),
          munitPrint(expected),
          munitComparisonHandler(obtained, expected),
          munitPrint(clue),
          printObtainedAsStripMargin = false,
        )
        // try with `.toString` in case `munitPrint()` produces identical formatting for both values.
        Diffs.assertNoDiff(
          obtained.toString(),
          expected.toString(),
          munitComparisonHandler(obtained, expected),
          munitPrint(clue),
          printObtainedAsStripMargin = false,
        )
        failComparison(
          s"values are not equal even if they have the same `toString()`: $obtained",
          obtained,
          expected,
        )
    }

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
