package mdlink

import io.circe.syntax.*
import io.circe.parser.*
import io.circe.generic.auto.*

class MatcherSuite extends RoundTripSuite[Matcher]:
  import Matcher.*

  roundtrip {
    // Regular Expressions are not ever equal to each other
    case (Pattern(found), Pattern(expected)) =>
      assert(found.toString == expected.toString)

    case (found, expected) => assertEquals(found, expected)
  }
