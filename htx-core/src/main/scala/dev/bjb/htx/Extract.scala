package dev.bjb.htx

import cats.Eq
import cats.implicits.*
import org.scalacheck.Arbitrary
import org.scalacheck.Gen

case class Extract(
    selector: Selector,
    attribute: Option[String],
    fallback: Option[String],
)

object Extract:
  def apply(selector: Selector): Extract = Extract(selector, None, None)

  given Eq[Extract] = Eq.instance { (a, b) =>
    a.selector === b.selector &&
    a.fallback === b.fallback &&
    a.attribute === b.attribute
  }
  given Arbitrary[Extract] = Arbitrary {
    Gen
      .zip(
        Arbitrary.arbitrary[Selector],
        Gen.option(Gen.alphaStr),
        Gen.option(Gen.alphaStr),
      )
      .map(Extract(_, _, _))
  }
