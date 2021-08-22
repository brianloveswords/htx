package mdlink

import io.circe.syntax.*
import io.circe.parser.*
import io.circe.generic.auto.*

class ExtractorSuite extends RoundTripSuite[Extractor]:
  import Extractor.*

  roundtrip
