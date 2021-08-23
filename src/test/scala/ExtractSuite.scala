package mdlink

import io.circe.generic.auto.*

class ExtractSuite extends RoundTripSuite[Extract]:
  roundtrip
