package mdlink

import cats.effect.IO
import munit.CatsEffectSuite
import scala.concurrent.duration.*

class PureSoupSuite extends CatsEffectSuite:
  test("time test") {
    IO.pure("rad").delayBy(1.second).flatMap { _ =>
      // how do I ensure this was scheduled for 1 second later?
      IO { assert(true) }
    }
  }
