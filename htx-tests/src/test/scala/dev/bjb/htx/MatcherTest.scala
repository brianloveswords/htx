package dev.bjb.htx

import io.circe.syntax.*
import io.circe.parser.*
import io.circe.generic.auto.*

class MatcherTest extends RoundTripSuite[Matcher]:
  import Matcher.*

  roundtrip
