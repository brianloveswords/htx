package main

import cats.effect.IO
import cats.effect.SyncIO
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.Prop
import org.scalacheck.effect.PropF
import org.scalacheck.Gen

class ExampleSuite extends CatsEffectSuite with ScalaCheckEffectSuite:
  test("regular test") {
    assert(true)
  }

  test("Prop test") {
    Prop.forAll { (x: Int) =>
      assertEquals(x + 1, x + 1)
    }
  }

  test("PropF test") {
    PropF.forAllF(Gen.posNum[Int]) { x =>
      double(x).map(res => assert(res == x * 2))
    }
  }
