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

class ConfigTemplateSuite extends CatsEffectSuite:
  test("cool") { 1 == 1 }
