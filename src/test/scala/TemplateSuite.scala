package main

import io.circe.generic.auto.*

class TemplateSuite extends RoundTripSuite[Template]:
  roundtrip
