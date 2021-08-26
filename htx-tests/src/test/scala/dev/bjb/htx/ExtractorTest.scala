package dev.bjb.htx

import io.circe.generic.auto.*
import io.circe.parser.*

class ExtractorTest extends RoundTripSuite[Extractor]:
  roundtrip
