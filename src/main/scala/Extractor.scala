package mdlink

import cats.Eq
import cats.syntax.*
import io.circe.generic.auto.*
import org.scalacheck.Arbitrary
import org.scalacheck.Gen

case class Extractor(
    selector: String,
    fallback: Option[String],
)

object Extractor:
  given Eq[Extractor] = Eq.fromUniversalEquals
  given Arbitrary[Extractor] = Arbitrary {
    for
      a1 <- Gen.alphaStr
      a2 <- Gen.option(Gen.alphaStr)
    yield Extractor(a1, a2)
  }
