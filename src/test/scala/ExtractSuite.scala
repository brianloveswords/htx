package mdlink

import io.circe.generic.auto.*
import io.circe.parser.*

class ExtractSuite extends RoundTripSuite[Extract]:
  roundtrip
