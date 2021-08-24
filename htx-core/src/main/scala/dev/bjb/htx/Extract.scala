package dev.bjb.htx

import cats.Eq
import cats.implicits.*
import org.scalacheck.Arbitrary
import org.scalacheck.Gen

case class Extract(
    selector: Selector,
    fallback: Option[String],
)

object Extract:
  given Eq[Extract] = Eq.instance { (a, b) =>
    a.selector === b.selector &&
    a.fallback === b.fallback
  }
  given Arbitrary[Extract] = Arbitrary {
    Gen
      .zip(Arbitrary.arbitrary[Selector], Gen.option(Gen.alphaStr))
      .map(Extract(_, _))
  }
