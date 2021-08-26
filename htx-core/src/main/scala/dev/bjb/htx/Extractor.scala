package dev.bjb.htx

import cats.Eq
import cats.implicits.*
import org.scalacheck.Arbitrary
import org.scalacheck.Gen

case class Extractor(
    selector: Selector,
    attribute: Option[String],
    fallback: Option[String],
)

object Extractor:
  def apply(selector: Selector): Extractor = Extractor(selector, None, None)
  def unsafe(text: String): Extractor =
    Extractor(Selector.unsafe(text), None, None)

  given Eq[Extractor] = Eq.instance { (a, b) =>
    a.selector === b.selector &&
    a.fallback === b.fallback &&
    a.attribute === b.attribute
  }
  given Arbitrary[Extractor] = Arbitrary {
    Gen
      .zip(
        Arbitrary.arbitrary[Selector],
        Gen.option(Gen.alphaStr),
        Gen.option(Gen.alphaStr),
      )
      .map(Extractor(_, _, _))
  }
