package main

import io.circe.syntax.*
import io.circe.parser.*
import io.circe.generic.auto.*

class MatcherSuite extends RoundTripSuite[Matcher]:
  import Matcher.*

  roundtrip
