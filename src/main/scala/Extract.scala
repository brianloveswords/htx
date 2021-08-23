package mdlink

import cats.Eq
import org.scalacheck.Arbitrary
import org.scalacheck.Gen

case class Extract(
    selector: String,
    fallback: Option[String],
)

object Extract:
  given Eq[Extract] = Eq.fromUniversalEquals
  given Arbitrary[Extract] = Arbitrary {
    Gen.zip(Gen.alphaStr, Gen.option(Gen.alphaStr)).map(Extract(_, _))
  }
