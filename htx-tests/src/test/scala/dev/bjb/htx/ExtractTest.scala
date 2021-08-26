package dev.bjb.htx

import io.circe.generic.auto.*
import io.circe.parser.*

class ExtractTest extends RoundTripSuite[Extractor]:
  roundtrip
